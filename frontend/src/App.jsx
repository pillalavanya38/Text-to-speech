
import "./App.css";
import { useEffect, useRef, useState } from "react";
import Login from "./Login";

function App() {
  const [loggedInUser, setLoggedInUser] = useState(
    localStorage.getItem("ttsUser")
  );

  const [text, setText] = useState("");
  const [language, setLanguage] = useState("en-IN");
  const [voice, setVoice] = useState("female");
  const [isSpeaking, setIsSpeaking] = useState(false);
  const [hasGenerated, setHasGenerated] = useState(false);
  const [audioUrl, setAudioUrl] = useState("");

  const audioRef = useRef(null);

  const maxCharacters = 1000;

  const API_URL =
    import.meta.env.VITE_API_URL || "http://localhost:8080";

  const languages = [
    { value: "en-IN", label: "English" },
    { value: "hi-IN", label: "Hindi" },
    { value: "gu-IN", label: "Gujarati" },
    { value: "mr-IN", label: "Marathi" },
    { value: "es-ES", label: "Spanish" },
    { value: "fr-FR", label: "French" },
    { value: "de-DE", label: "German" },
  ];

  const voices = [
    { value: "female", label: "Female" },
    { value: "male", label: "Male" },
  ];

  const handleLogin = (email) => {
    setLoggedInUser(email);
  };

  const handleLogout = () => {
    localStorage.removeItem("ttsUser");
    setLoggedInUser(null);

    setText("");
    setHasGenerated(false);

    if (audioUrl) {
      URL.revokeObjectURL(audioUrl);
    }

    setAudioUrl("");
  };

  useEffect(() => {
    return () => {
      if (audioUrl) {
        URL.revokeObjectURL(audioUrl);
      }
    };
  }, [audioUrl]);

  const handleGenerateSpeech = async () => {
    if (!text.trim()) {
      alert("Please enter some text.");
      return;
    }

    if (text.length > maxCharacters) {
      alert("Text cannot exceed 1000 characters.");
      return;
    }

    try {
      setIsSpeaking(true);
      setHasGenerated(false);

      if (audioUrl) {
        URL.revokeObjectURL(audioUrl);
        setAudioUrl("");
      }

      const response = await fetch(
        `${API_URL}/api/tts/generate`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            text: text,
            language: language,
            voice: voice,
          }),
        }
      );

      const data = await response.json();

      if (!response.ok || !data.success) {
        throw new Error(
          data.message || "Unable to generate speech."
        );
      }

      const generatedAudioUrl = `${API_URL}${data.audioUrl}`;

      setAudioUrl(generatedAudioUrl);
      setHasGenerated(true);
    } catch (error) {
      console.error("TTS Error:", error);

      alert(
        error.message || "Unable to generate speech."
      );
    } finally {
      setIsSpeaking(false);
    }
  };

  const handleClear = () => {
    setText("");
    setHasGenerated(false);

    if (audioUrl) {
      URL.revokeObjectURL(audioUrl);
    }

    setAudioUrl("");

    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current.currentTime = 0;
    }
  };

  const wordCount =
    text.trim() === ""
      ? 0
      : text.trim().split(/\s+/).length;

  const characterCount = text.length;

  if (!loggedInUser) {
    return <Login onLogin={handleLogin} />;
  }

  return (
    <div className="app">
      <div className="container">

        <div className="top-bar">
          <div>
            <h1>Text-to-Speech Application</h1>
            <p className="subtitle">
              Convert your text into natural speech
            </p>
          </div>

          <button
            className="logout-button"
            onClick={handleLogout}
          >
            Logout
          </button>
        </div>

        <div className="user-info">
          Logged in as: <strong>{loggedInUser}</strong>
        </div>

        <div className="form-group">
          <label htmlFor="text">
            Enter Text
          </label>

          <textarea
            id="text"
            value={text}
            onChange={(e) => {
              if (e.target.value.length <= maxCharacters) {
                setText(e.target.value);
                setHasGenerated(false);
              }
            }}
            placeholder="Enter the text you want to convert into speech..."
            rows="8"
          />

          <div className="counter">
            <span>
              Words: {wordCount}
            </span>

            <span>
              Characters: {characterCount}/{maxCharacters}
            </span>
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="language">
            Select Language
          </label>

          <select
            id="language"
            value={language}
            onChange={(e) => {
              setLanguage(e.target.value);
              setHasGenerated(false);
            }}
          >
            {languages.map((item) => (
              <option
                key={item.value}
                value={item.value}
              >
                {item.label}
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="voice">
            Select Voice
          </label>

          <select
            id="voice"
            value={voice}
            onChange={(e) => {
              setVoice(e.target.value);
              setHasGenerated(false);
            }}
          >
            {voices.map((item) => (
              <option
                key={item.value}
                value={item.value}
              >
                {item.label}
              </option>
            ))}
          </select>
        </div>

        <div className="button-container">
          <button
            className="generate-button"
            onClick={handleGenerateSpeech}
            disabled={isSpeaking}
          >
            {isSpeaking
              ? "Generating..."
              : "Generate Speech"}
          </button>

          <button
            className="clear-button"
            onClick={handleClear}
          >
            Clear
          </button>
        </div>

        {hasGenerated && (
          <div className="success-message">
            Speech generated successfully.
          </div>
        )}

        {audioUrl && (
          <div className="audio-section">
            <h2>Generated Speech</h2>

            <audio
              ref={audioRef}
              controls
              src={audioUrl}
              className="audio-player"
            >
              Your browser does not support the audio element.
            </audio>

            <div className="download-container">
              <a
                href={`${audioUrl}/download`}
                className="download-button"
                download
              >
                Download Audio
              </a>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;

