use axum::{http::StatusCode, response::{IntoResponse, Response}, Json};
use serde_json::{json, Value};

pub struct AppError {
    pub status: StatusCode,
    pub message: String,
}

impl AppError {
    pub fn new(status: StatusCode, message: impl Into<String>) -> Self {
        Self {
            status,
            message: message.into(),
        }
    }
}

impl IntoResponse for AppError {
    fn into_response(self) -> Response {
        let body: Value = json!({ "error": self.message });
        (self.status, Json(body)).into_response()
    }
}
