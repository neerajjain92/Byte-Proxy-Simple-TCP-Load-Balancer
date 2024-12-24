from http.server import HTTPServer, BaseHTTPRequestHandler
import os
import time

class SimpleServer(BaseHTTPRequestHandler):
    def do_GET(self):
        print(f"Received request for path: {self.path}")
        if self.path == '/health':
            self.send_response(200)
            self.send_header('Content-type', 'text/plain')
            self.end_headers()
            self.wfile.write(b"OK")
            return

        self.send_response(200)
        self.send_header('Content-type', 'text/plain')
        self.end_headers()
        server_id = os.environ.get('SERVER_ID', 'unknown')
        response = f"Hello, I am server{server_id}! Current time: {time.strftime('%H:%M:%S')}\n"
        self.wfile.write(response.encode())

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 8000))
    server = HTTPServer(('0.0.0.0', port), SimpleServer)
    print(f'Starting server on port {port}...')
    server.serve_forever()