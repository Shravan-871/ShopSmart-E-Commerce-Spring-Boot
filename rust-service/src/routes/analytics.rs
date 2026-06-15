use crate::error::AppError;
use crate::spring_client::SpringClient;
use axum::{extract::State, Json};
use serde::Serialize;
use serde_json::{json, Value};

#[derive(Serialize)]
pub struct AnalyticsSummary {
    pub total_products: i64,
    pub average_price: f64,
    pub total_stock: i64,
    pub low_stock_count: i64,
    pub source: &'static str,
}

pub async fn summary(State(client): State<SpringClient>) -> Result<Json<Value>, AppError> {
    let stats = client.get_stats().await?;
    let low_stock_count = client.low_stock_count(10).await.unwrap_or(0);

    let summary = AnalyticsSummary {
        total_products: stats.total_products,
        average_price: stats.average_price,
        total_stock: stats.total_stock,
        low_stock_count,
        source: "rust-service",
    };

    Ok(Json(json!(summary)))
}
