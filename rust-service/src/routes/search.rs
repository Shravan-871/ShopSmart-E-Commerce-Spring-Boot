use crate::error::AppError;
use crate::spring_client::SpringClient;
use axum::{
    extract::{Query, State},
    Json,
};
use serde::Deserialize;
use serde_json::Value;

#[derive(Deserialize)]
pub struct SearchQuery {
    pub q: String,
}

#[derive(Deserialize)]
pub struct LowStockQuery {
    #[serde(default = "default_threshold")]
    pub threshold: i32,
}

fn default_threshold() -> i32 {
    10
}

pub async fn search(
    State(client): State<SpringClient>,
    Query(params): Query<SearchQuery>,
) -> Result<Json<Vec<Value>>, AppError> {
    let products = client.search_products(&params.q).await?;
    let json: Vec<Value> = products
        .into_iter()
        .map(|p| {
            serde_json::json!({
                "id": p.id,
                "name": p.name,
                "price": p.price,
                "category": p.category,
                "stock": p.stock,
                "description": p.description
            })
        })
        .collect();
    Ok(Json(json))
}

pub async fn low_stock(
    State(client): State<SpringClient>,
    Query(params): Query<LowStockQuery>,
) -> Result<Json<Vec<Value>>, AppError> {
    let products = client.low_stock(params.threshold).await?;
    let json: Vec<Value> = products
        .into_iter()
        .map(|p| {
            serde_json::json!({
                "id": p.id,
                "name": p.name,
                "price": p.price,
                "category": p.category,
                "stock": p.stock,
                "description": p.description
            })
        })
        .collect();
    Ok(Json(json))
}
