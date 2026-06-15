use axum::response::Html;

pub async fn index() -> Html<&'static str> {
    Html(
        r#"<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>ShopSmart Rust Service</title>
  <style>
    body { font-family: system-ui, sans-serif; max-width: 640px; margin: 3rem auto; padding: 0 1rem; color: #1a1a2e; }
    h1 { color: #6c63ff; }
    a { color: #6c63ff; }
    code { background: #f0f2f5; padding: 0.15rem 0.4rem; border-radius: 4px; }
    li { margin: 0.5rem 0; }
  </style>
</head>
<body>
  <h1>🦀 ShopSmart Rust Service</h1>
  <p>Analytics sidecar for Spring Boot. The main shop UI is on port <strong>8080</strong>.</p>
  <ul>
    <li><a href="http://localhost:8080">Shop UI → http://localhost:8080</a></li>
    <li><a href="/health"><code>GET /health</code></a> — liveness</li>
    <li><a href="/api/v1/analytics/summary"><code>GET /api/v1/analytics/summary</code></a> — stats</li>
    <li><a href="/api/v1/search?q=laptop"><code>GET /api/v1/search?q=</code></a> — product search</li>
    <li><a href="/api/v1/products/low-stock?threshold=10"><code>GET /api/v1/products/low-stock</code></a></li>
  </ul>
  <p><small>Requires Spring Boot running on <code>localhost:8080</code> for data endpoints.</small></p>
</body>
</html>"#,
    )
}
