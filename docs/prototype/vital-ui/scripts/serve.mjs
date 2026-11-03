// Tiny static server (no dependencies) so Playwright can load ES modules over http.
import http from "node:http";
import { readFile } from "node:fs/promises";
import { extname, join, normalize } from "node:path";
const types = { ".html": "text/html", ".js": "text/javascript", ".mjs": "text/javascript", ".css": "text/css", ".woff2": "font/woff2", ".png": "image/png", ".json": "application/json" };
export function serve(root = process.cwd(), port = 5179) {
  const server = http.createServer(async (req, res) => {
    try {
      let p = normalize(decodeURIComponent(new URL(req.url, "http://x").pathname));
      if (p.endsWith("/")) p += "index.html";
      const data = await readFile(join(root, p));
      res.writeHead(200, { "content-type": types[extname(p)] ?? "application/octet-stream" });
      res.end(data);
    } catch { res.writeHead(404); res.end("not found"); }
  });
  return new Promise((ok) => server.listen(port, () => ok(server)));
}
if (import.meta.url === `file://${process.argv[1]}`) { await serve(); console.log("http://localhost:5179"); }
