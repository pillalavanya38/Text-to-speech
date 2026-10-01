
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

  // Local Spring Boot backend
  const API_URL = "https://text-to-speech-backend-2lf8.onrender.com";

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

      const generatedAudioUrl =
        `${API_URL}${data.audioUrl}`;

      setAudioUrl(generatedAudioUrl);
      setHasGenerated(true);

    } catch (error) {
      console.error("TTS Error:", error);

      alert(
        error.message ||
        "Unable to generate speech."
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

  if (!loggedInUser) {
    return <Login onLogin={handleLogin} />;
  }

  return (
    <div className="app-container">

      <header className="app-header">

        <div>
          <h1>Text-to-Speech Application</h1>

          <p>
            Convert your text into natural speech
          </p>
        </div>

        <div className="user-section">

          <span>{loggedInUser}</span>

          <button
            className="logout-button"
            onClick={handleLogout}
          >
            Logout
          </button>

        </div>

      </header>

      <main className="tts-container">

        <div className="tts-card">

          <div className="input-section">

            <label htmlFor="text">
              Enter Text
            </label>

            <textarea
              id="text"
              value={text}
              onChange={(e) =>
                setText(e.target.value)
              }
              placeholder="Enter the text you want to convert into speech..."
              maxLength={maxCharacters}
              rows="8"
            />

            <div className="text-counter">

              <span>
                Characters: {text.length}/{maxCharacters}
              </span>

              <span>
                Words:{" "}
                {text.trim()
                  ? text.trim().split(/\s+/).length
                  : 0}
              </span>

            </div>

          </div>

          <div className="selection-section">

            <div className="selection-group">

              <label htmlFor="language">
                Language
              </label>

              <select
                id="language"
                value={language}
                onChange={(e) =>
                  setLanguage(e.target.value)
                }
              >

                {languages.map((lang) => (
                  <option
                    key={lang.value}
                    value={lang.value}
                  >
                    {lang.label}
                  </option>
                ))}

              </select>

            </div>

            <div className="selection-group">

              <label htmlFor="voice">
                Voice
              </label>

              <select
                id="voice"
                value={voice}
                onChange={(e) =>
                  setVoice(e.target.value)
                }
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

          </div>

          <div className="button-section">

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

          {hasGenerated && audioUrl && (

            <div className="audio-section">

              <h3>Generated Speech</h3>

              <audio
                ref={audioRef}
                controls
                src={audioUrl}
              >
                Your browser does not support
                the audio element.
              </audio>

              <a
                className="download-button"
                href={`${audioUrl}/download`}
                download
              >
                Download Audio
              </a>

            </div>

          )}

        </div>

      </main>

    </div>
  );
}

export default App;

