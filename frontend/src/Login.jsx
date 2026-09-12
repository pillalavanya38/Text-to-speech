import { useState } from "react";

function Login({ onLogin }) {
  const [isSignUp, setIsSignUp] = useState(false);

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();

    setError("");
    setMessage("");

    // SIGN UP
    if (isSignUp) {
      if (password !== confirmPassword) {
        setError("Passwords do not match.");
        return;
      }

      if (username.length < 3) {
        setError("Username must be at least 3 characters.");
        return;
      }

      if (password.length < 6) {
        setError("Password must be at least 6 characters.");
        return;
      }

      // Save demo user
      localStorage.setItem(
        "ttsUsername",
        username
      );

      localStorage.setItem(
        "ttsPassword",
        password
      );

      setMessage(
        "Account created successfully! Please login."
      );

      setIsSignUp(false);
      setPassword("");
      setConfirmPassword("");

      return;
    }

    // LOGIN
    const savedUsername =
      localStorage.getItem("ttsUsername");

    const savedPassword =
      localStorage.getItem("ttsPassword");

    // Default demo account
    const validUsername =
      savedUsername || "admin";

    const validPassword =
      savedPassword || "admin123";

    if (
      username === validUsername &&
      password === validPassword
    ) {
      localStorage.setItem(
        "ttsLoggedIn",
        "true"
      );

      onLogin();

    } else {
      setError(
        "Invalid username or password."
      );
    }
  };

  const switchMode = () => {
    setIsSignUp(!isSignUp);
    setError("");
    setMessage("");
    setPassword("");
    setConfirmPassword("");
  };

  return (
    <div className="login-page">

      <div className="login-card">

        <div className="login-logo">
          🔊
        </div>

        <h1>
          Text to Speech
        </h1>

        <p className="login-subtitle">
          {isSignUp
            ? "Create your account"
            : "Login to continue"}
        </p>

        <form onSubmit={handleSubmit}>

          <label htmlFor="username">
            Username
          </label>

          <input
            id="username"
            type="text"
            value={username}
            onChange={(e) => {
              setUsername(e.target.value);
              setError("");
            }}
            placeholder="Enter username"
            required
          />

          <label htmlFor="password">
            Password
          </label>

          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => {
              setPassword(e.target.value);
              setError("");
            }}
            placeholder="Enter password"
            required
          />

          {isSignUp && (
            <>
              <label htmlFor="confirmPassword">
                Confirm Password
              </label>

              <input
                id="confirmPassword"
                type="password"
                value={confirmPassword}
                onChange={(e) => {
                  setConfirmPassword(
                    e.target.value
                  );
                  setError("");
                }}
                placeholder="Confirm password"
                required
              />
            </>
          )}

          {error && (
            <p className="login-error">
              {error}
            </p>
          )}

          {message && (
            <p className="login-success">
              {message}
            </p>
          )}

          <button
            type="submit"
            className="login-button"
          >
            {isSignUp
              ? "📝 Sign Up"
              : "🔐 Login"}
          </button>

        </form>

        <div className="login-switch">

          {isSignUp
            ? "Already have an account?"
            : "Don't have an account?"}

          <button
            type="button"
            className="switch-button"
            onClick={switchMode}
          >
            {isSignUp
              ? "Login"
              : "Sign Up"}
          </button>

        </div>

      </div>

    </div>
  );
}

export default Login;