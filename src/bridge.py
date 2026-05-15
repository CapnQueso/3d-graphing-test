import http.server
import socketserver

PORT = 8080
Handler = http.server.SimpleHTTPRequestHandler
# This allows the HTML to fetch the data.txt file while the Java program writes to it
Handler.extensions_map.update({'.txt': 'text/plain'})

with socketserver.TCPServer(("", PORT), Handler) as httpd:
    print(f"Server started at http://localhost:{PORT}")
    print("Keep this window open. Open http://localhost:8081 in Firefox.")
    httpd.serve_forever()