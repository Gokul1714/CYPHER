$port = 8085
$htmlPath = "C:\Users\dhars\.gemini\antigravity\brain\b63fa111-1b9f-49b1-b7a8-758057653faa\apaar_verify_app.html"
$listener = New-Object System.Net.HttpListener
$listener.Prefixes.Add("http://127.0.0.1:$port/")
$listener.Prefixes.Add("http://localhost:$port/")

try {
    $listener.Start()
    Write-Host "CYPHER Web Server running at http://localhost:$port/"
    
    while ($listener.IsListening) {
        try {
            $context = $listener.GetContext()
            $request = $context.Request
            $response = $context.Response

            if (Test-Path $htmlPath) {
                $bytes = [System.IO.File]::ReadAllBytes($htmlPath)
                $response.ContentType = "text/html; charset=utf-8"
                $response.ContentLength64 = $bytes.Length
                $response.OutputStream.Write($bytes, 0, $bytes.Length)
            } else {
                $msg = [System.Text.Encoding]::UTF8.GetBytes("File not found")
                $response.StatusCode = 404
                $response.OutputStream.Write($msg, 0, $msg.Length)
            }
            $response.Close()
        } catch {
            # Continue listening even if an individual client disconnects
        }
    }
} finally {
    $listener.Stop()
}
