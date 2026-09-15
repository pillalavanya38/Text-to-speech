
import { useState } from "react";
import "./App.css";

function Login({ onLogin }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = (e) => {
    e.preventDefault();

    if (!email.trim() || !password.trim()) {
      alert("Please enter email and password.");
      return;
    }

    // Simple frontend login for the internship project
    localStorage.setItem("ttsUser", email.trim());

    onLogin(email.trim());
  };

  return (
    <div className="app">
      <div className="container login-container">
        <h1>Text-to-Speech</h1>

        <p className="subtitle">
          Login to continue
        </p>

        <form onSubmit={handleLogin}>
          <div className="form-group">
            <label htmlFor="email">
              Email
            </label>

            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter your email"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">
              Password
            </label>

            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              required
            />
          </div>

          <button
            type="submit"
            className="generate-button login-button"
          >
            Login
          </button>
        </form>
      </div>
    </div>
  );
}

export default Login;

