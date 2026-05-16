import http.server
import socketserver
import urllib.parse
import os

PORT = 8000

class SimulationBridgeHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        parsed_url = urllib.parse.urlparse(self.path)
        
        # Catch the warp speed signal from the UI slider
        if parsed_url.path == '/set_timestep':
            query_params = urllib.parse.parse_qs(parsed_url.query)
            if 'dt' in query_params:
                dt_val = query_params['dt'][0]
                try:
                    # Save the new calculation time step out to disk for Java to harvest
                    with open("src/timestep.txt", "w") as f:
                        f.write(dt_val)
                except Exception as e:
                    print(f"Error saving timestep parameters: {e}")
            
            # Send a fast 200 OK back to keep the browser responsive
            self.send_response(200)
            self.send_header('Content-type', 'text/plain')
            self.end_headers()
            self.wfile.write(b"OK")
            return
            
        # Fallback to standard static file server behavior for HTML/text data feeds
        return super().do_GET()

# Instruct the response map to cleanly transmit plain text vectors
SimulationBridgeHandler.extensions_map.update({'.txt': 'text/plain'})

with socketserver.TCPServer(("", PORT), SimulationBridgeHandler) as httpd:
    print(f"Server started at http://localhost:{PORT}")
    httpd.serve_forever()