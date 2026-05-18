import os
from datetime import datetime, timezone

import requests
from flask import Flask, jsonify, request

app = Flask(__name__)

OPENWEATHER_URL = "https://api.openweathermap.org/data/2.5/forecast"
REQUEST_TIMEOUT_SECONDS = 10


def _parse_required_float(param_name: str) -> float:
    raw_value = request.args.get(param_name)
    if raw_value is None:
        raise ValueError(f"Missing required query parameter: {param_name}")
    try:
        return float(raw_value)
    except ValueError as exc:
        raise ValueError(f"Invalid {param_name}: must be a number") from exc


def _extract_point(block: dict) -> dict:
    main = block.get("main") or {}
    temp = main.get("temp")
    if temp is None:
        raise ValueError("Missing temperature in OpenWeatherMap response")
    weather_items = block.get("weather") or []
    first_weather = weather_items[0] if weather_items else {}
    return {
        "temperature_c": float(temp),
        "description": str(first_weather.get("description", "")).strip(),
        "icon_id": str(first_weather.get("icon", "")).strip(),
    }


def _closest_forecast_point(forecasts: list, target_epoch_sec: int) -> dict:
    if not forecasts:
        raise ValueError("OpenWeatherMap forecast list is empty")

    valid_items = [item for item in forecasts if isinstance(item.get("dt"), int)]
    if not valid_items:
        raise ValueError("OpenWeatherMap forecast list has no valid timestamps")

    # Prefer the nearest timestamp. If equally close, prefer the future point.
    return min(
        valid_items,
        key=lambda item: (abs(item["dt"] - target_epoch_sec), item["dt"] < target_epoch_sec),
    )


@app.get("/health")
def health() -> tuple:
    return jsonify({"status": "ok"}), 200


@app.get("/weather")
def weather() -> tuple:
    try:
        latitude = _parse_required_float("lat")
        longitude = _parse_required_float("lon")
    except ValueError as exc:
        return jsonify({"error": str(exc)}), 400

    api_key = os.environ.get("OPENWEATHER_API_KEY")
    if not api_key:
        return jsonify({"error": "Server is missing OPENWEATHER_API_KEY"}), 500

    params = {
        "lat": latitude,
        "lon": longitude,
        "appid": api_key,
        "units": "metric",
    }

    try:
        response = requests.get(
            OPENWEATHER_URL,
            params=params,
            timeout=REQUEST_TIMEOUT_SECONDS,
        )
        response.raise_for_status()
        payload = response.json()
    except requests.HTTPError as exc:
        status_code = exc.response.status_code if exc.response is not None else 502
        return jsonify({"error": "OpenWeatherMap request failed"}), status_code
    except requests.RequestException:
        return jsonify({"error": "Unable to reach OpenWeatherMap"}), 502
    except ValueError:
        return jsonify({"error": "Invalid JSON from OpenWeatherMap"}), 502

    forecast_list = payload.get("list")
    if not isinstance(forecast_list, list) or not forecast_list:
        return jsonify({"error": "OpenWeatherMap response missing required fields"}), 502

    try:
        now_epoch_sec = int(datetime.now(timezone.utc).timestamp())
        current_forecast = _closest_forecast_point(forecast_list, now_epoch_sec)
        plus_1h_forecast = _closest_forecast_point(forecast_list, now_epoch_sec + 3600)
        plus_2h_forecast = _closest_forecast_point(forecast_list, now_epoch_sec + 7200)

        result = {
            "latitude": latitude,
            "longitude": longitude,
            "fetched_at": now_epoch_sec,
            "current": _extract_point(current_forecast),
            "plus_1h": _extract_point(plus_1h_forecast),
            "plus_2h": _extract_point(plus_2h_forecast),
        }
    except ValueError as exc:
        return jsonify({"error": str(exc)}), 502

    return jsonify(result), 200


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)

