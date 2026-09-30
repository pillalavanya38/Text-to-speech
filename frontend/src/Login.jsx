
import { useState } from "react";
import "./App.css";

function Login({ onLogin }) {
  const [isSignup, setIsSignup] = useState(false);

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();

    // SIGN UP
    if (isSignup) {
      if (
        !name.trim() ||
        !email.trim() ||
        !password.trim() ||
        !confirmPassword.trim()
      ) {
        alert("Please fill in all fields.");
        return;
      }

      if (password !== confirmPassword) {
        alert("Passwords do not match.");
        return;
      }

      const existingUser = localStorage.getItem("ttsRegisteredUser");

      if (existingUser) {
        const user = JSON.parse(existingUser);

        if (email.trim() === user.email) {
          alert("An account with this email already exists.");
          return;
        }
      }

      const user = {
        name: name.trim(),
        email: email.trim(),
        password: password,
      };

      localStorage.setItem(
        "ttsRegisteredUser",
        JSON.stringify(user)
      );

      alert("Sign up successful! Please login.");

      setIsSignup(false);
      setName("");
      setEmail("");
      setPassword("");
      setConfirmPassword("");

      return;
    }

    // LOGIN
    if (!email.trim() || !password.trim()) {
      alert("Please enter email and password.");
      return;
    }

    const savedUser = localStorage.getItem("ttsRegisteredUser");

    if (!savedUser) {
      alert("No account found. Please sign up first.");
      return;
    }

    const user = JSON.parse(savedUser);

    if (
      email.trim() !== user.email ||
      password !== user.password
    ) {
      alert("Invalid email or password.");
      return;
    }

    localStorage.setItem("ttsUser", email.trim());
    onLogin(email.trim());
  };

  return (
    <div className="login-page">
      <div className="login-card">

        <div className="tts-icon">
          🎙️
        </div>

        <h1>Text-to-Speech</h1>

        <p className="login-subtitle">
          {isSignup
            ? "Create your account to get started"
            : "Convert your text into natural speech"}
        </p>

        <form onSubmit={handleSubmit}>

          {isSignup && (
            <div className="login-form-group">
              <label htmlFor="name">Full Name</label>

              <input
                id="name"
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Enter your name"
                required
              />
            </div>
          )}

          <div className="login-form-group">
            <label htmlFor="email">Email Address</label>

            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter your email"
              required
            />
          </div>

          <div className="login-form-group">
            <label htmlFor="password">Password</label>

            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              required
            />
          </div>

          {isSignup && (
            <div className="login-form-group">
              <label htmlFor="confirmPassword">
                Confirm Password
              </label>

              <input
                id="confirmPassword"
                type="password"
                value={confirmPassword}
                onChange={(e) =>
                  setConfirmPassword(e.target.value)
                }
                placeholder="Confirm your password"
                required
              />
            </div>
          )}

          <button
            type="submit"
            className="login-submit-button"
          >
            {isSignup ? "Create Account" : "Login"}
          </button>
        </form>

        <div className="login-divider">
          <span>OR</span>
        </div>

        <p className="signup-text">
          {isSignup
            ? "Already have an account?"
            : "Don't have an account?"}

          <button
            type="button"
            className="signup-link"
            onClick={() => {
              setIsSignup(!isSignup);
              setName("");
              setEmail("");
              setPassword("");
              setConfirmPassword("");
            }}
          >
            {isSignup ? " Login" : " Sign Up"}
          </button>
        </p>

        <p className="login-footer">
          🎧 Simple • Fast • Accessible
        </p>

      </div>
    </div>
  );
}

export default Login;

