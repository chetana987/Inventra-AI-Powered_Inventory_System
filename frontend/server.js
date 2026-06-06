const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;
const API_URL = process.env.API_URL || 'http://localhost:8080';

app.use('/api', createProxyMiddleware({
  target: API_URL,
  changeOrigin: true
}));

app.use(express.static(path.join(__dirname, 'dist/frontend/browser')));

app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist/frontend/browser/index.html'));
});

app.listen(PORT, () => {
  console.log(`Frontend server running on port ${PORT}, proxying API to ${API_URL}`);
});
