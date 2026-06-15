mod error;
mod routes;
mod spring_client;

use axum::{routing::get, Router};
use spring_client::SpringClient;
use std::net::SocketAddr;
use tower_http::cors::{Any, CorsLayer};

#[tokio::main]
async fn main() {
    let port: u16 = std::env::var("RUST_PORT")
        .ok()
        .and_then(|p| p.parse().ok())
        .unwrap_or(8081);

    let spring_url = std::env::var("SPRING_BASE_URL")
        .unwrap_or_else(|_| "http://localhost:8080".into());

    let client = SpringClient::new(spring_url);

    let cors = CorsLayer::new()
        .allow_origin(Any)
        .allow_methods(Any)
        .allow_headers(Any);

    let app = Router::new()
        .route("/", get(routes::root::index))
        .route("/health", get(routes::health::health))
        .route(
            "/api/v1/analytics/summary",
            get(routes::analytics::summary),
        )
        .route("/api/v1/search", get(routes::search::search))
        .route(
            "/api/v1/products/low-stock",
            get(routes::search::low_stock),
        )
        .layer(cors)
        .with_state(client);

    let addr = SocketAddr::from(([0, 0, 0, 0], port));
    println!("ShopSmart Rust service listening on http://{addr}");
    let listener = tokio::net::TcpListener::bind(addr).await.unwrap();
    axum::serve(listener, app).await.unwrap();
}
