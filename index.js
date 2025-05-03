// Import necessary libraries
const express = require('express');
const cors = require('cors');

// Initialize the Express app
const app = express();

// Define the CORS options (you can set specific domains here)
const corsOptions = {
  origin: 'https://assessmatefinal-6cog.vercel.app', // Your frontend URL
  methods: ['GET', 'POST', 'PUT', 'DELETE'],       // Allowed methods
  allowedHeaders: ['Content-Type', 'Authorization'], // Allowed headers
  credentials: true,                               // Enable cookies and headers
};

// Enable CORS for all routes
app.use(cors(corsOptions));

// Middleware to parse JSON bodies
app.use(express.json());

// Example POST route for login (replace with your actual routes)
app.post('/api/auth/signin', (req, res) => {
  const { username, password } = req.body;

  // Add your authentication logic here
  res.json({ message: 'Successfully logged in!' });
});

// Start your backend server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
