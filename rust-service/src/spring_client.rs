use crate::error::AppError;
use axum::http::StatusCode;
use serde::Deserialize;
use serde_json::Value;

#[derive(Clone)]
pub struct SpringClient {
    base_url: String,
    http: reqwest::Client,
}

#[derive(Debug, Deserialize)]
pub struct ProductStats {
    #[serde(rename = "totalProducts")]
    pub total_products: i64,
    #[serde(rename = "averagePrice")]
    pub average_price: f64,
    #[serde(rename = "totalStock")]
    pub total_stock: i64,
}

#[derive(Debug, Deserialize)]
pub struct Product {
    pub id: Option<i64>,
    pub name: String,
    pub price: f64,
    pub category: String,
    pub stock: i32,
    pub description: Option<String>,
}

impl SpringClient {
    pub fn new(base_url: String) -> Self {
        Self {
            base_url: base_url.trim_end_matches('/').to_string(),
            http: reqwest::Client::new(),
        }
    }

    fn authed(&self, req: reqwest::RequestBuilder) -> reqwest::RequestBuilder {
        let user = std::env::var("SPRING_USERNAME").unwrap_or_else(|_| "admin".into());
        let pass = std::env::var("SPRING_PASSWORD").unwrap_or_else(|_| "admin123".into());
        req.basic_auth(user, Some(pass))
    }

    pub async fn get_stats(&self) -> Result<ProductStats, AppError> {
        let url = format!("{}/products/stats", self.base_url);
        self.authed(self.http.get(&url))
            .send()
            .await
            .map_err(|e| AppError::new(StatusCode::BAD_GATEWAY, e.to_string()))?
            .error_for_status()
            .map_err(|e| AppError::new(StatusCode::BAD_GATEWAY, e.to_string()))?
            .json()
            .await
            .map_err(|e| AppError::new(StatusCode::BAD_GATEWAY, e.to_string()))
    }

    pub async fn search_products(&self, q: &str) -> Result<Vec<Product>, AppError> {
        let url = format!("{}/products/search?name={}", self.base_url, urlencoding(q));
        self.fetch_products(&url).await
    }

    pub async fn low_stock(&self, threshold: i32) -> Result<Vec<Product>, AppError> {
        let url = format!(
            "{}/products/low-stock?threshold={}",
            self.base_url, threshold
        );
        self.fetch_products(&url).await
    }

    async fn fetch_products(&self, url: &str) -> Result<Vec<Product>, AppError> {
        self.authed(self.http.get(url))
            .send()
            .await
            .map_err(|e| AppError::new(StatusCode::BAD_GATEWAY, e.to_string()))?
            .error_for_status()
            .map_err(|e| AppError::new(StatusCode::BAD_GATEWAY, e.to_string()))?
            .json()
            .await
            .map_err(|e| AppError::new(StatusCode::BAD_GATEWAY, e.to_string()))
    }

    pub async fn low_stock_count(&self, threshold: i32) -> Result<i64, AppError> {
        Ok(self.low_stock(threshold).await?.len() as i64)
    }
}

fn urlencoding(s: &str) -> String {
    s.chars()
        .map(|c| match c {
            ' ' => "%20".to_string(),
            '&' => "%26".to_string(),
            '=' => "%3D".to_string(),
            _ if c.is_ascii_alphanumeric() || c == '-' || c == '_' || c == '.' || c == '~' => {
                c.to_string()
            }
            _ => format!("%{:02X}", c as u8),
        })
        .collect()
}

#[allow(dead_code)]
pub type JsonValue = Value;
