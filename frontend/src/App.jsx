
import { useEffect, useRef, useState } from "react";
import "./App.css";
import Login from "./Login";

// Render deployed Spring Boot backend
const API_BASE_URL = "https://text-to-speech-backend-2lf8.onrender.com";

function App() {
  // =========================
  // LOGIN STATE
  // =========================
  const [isLoggedIn, setIsLoggedIn] = useState(
    localStorage.getItem("ttsLoggedIn") === "true"
  );

  // =========================
  // TTS STATE
  // =========================
  const [text, setText] = useState("");
  const [language, setLanguage] = useState("en-US");
  const [voice, setVoice] = useState("female");
  const [voices, setVoices] = useState([]);
  const [isSpeaking, setIsSpeaking] = useState(false);
  const [hasGenerated, setHasGenerated] = useState(false);
  const [audioUrl, setAudioUrl] = useState("");

  const audioRef = useRef(null);

  const maxCharacters = 1000;

  const wordCount = text.trim()
    ? text.trim().split(/\s+/).length
    : 0;

  const characterCount = text.length;

  // =========================
  // LOGOUT
  // =========================
  const handleLogout = () => {
    localStorage.removeItem("ttsLoggedIn");

    setIsLoggedIn(false);
    setText("");
    setAudioUrl("");
    setHasGenerated(false);
    setIsSpeaking(false);
  };

  // =========================
  // LOAD BROWSER VOICES
  // =========================
  useEffect(() => {
    const loadVoices = () => {
      const availableVoices = window.speechSynthesis.getVoices();
      setVoices(availableVoices);
    };

    loadVoices();

    window.speechSynthesis.onvoiceschanged = loadVoices;

    return () => {
      window.speechSynthesis.onvoiceschanged = null;
    };
  }, []);

  // =========================
  // GENERATE SPEECH
  // =========================
  const handleGenerate = async () => {
    if (!text.trim()) {
      alert("Please enter some text first.");
      return;
    }

    try {
      setHasGenerated(false);
      setAudioUrl("");
      setIsSpeaking(false);

      // Stop previous audio
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current.currentTime = 0;
      }

      const response = await fetch(
        `${API_BASE_URL}/api/tts/generate`,
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

      const result = await response.json();

      if (!response.ok || !result.success) {
        alert(
          result.message ||
            "Unable to generate audio."
        );
        return;
      }

      // Convert backend relative URL into complete Render URL
      const generatedAudioUrl =
        API_BASE_URL + result.audioUrl;

      setAudioUrl(generatedAudioUrl);
      setHasGenerated(true);

      console.log(
        "Generated audio:",
        generatedAudioUrl
      );
    } catch (error) {
      console.error("Error:", error);

      alert(
        "Could not connect to the Spring Boot server."
      );
    }
  };

  // =========================
  // PLAY AUDIO
  // =========================
  const handlePlay = async () => {
    if (!audioUrl) {
      alert("Please generate speech first.");
      return;
    }

    const audio = audioRef.current;

    if (!audio) {
      alert("Audio player is not available.");
      return;
    }

    try {
      window.speechSynthesis.cancel();

      audio.load();

      await audio.play();

      setIsSpeaking(true);
    } catch (error) {
      console.error(
        "Audio playback error:",
        error
      );

      alert(
        "Unable to play the audio. Please try the Play button on the audio player."
      );
    }
  };

  // =========================
  // STOP AUDIO
  // =========================
  const handleStop = () => {
    window.speechSynthesis.cancel();

    const audio = audioRef.current;

    if (audio) {
      audio.pause();
      audio.currentTime = 0;
    }

    setIsSpeaking(false);
  };

  // =========================
  // CLEAR
  // =========================
  const handleClear = () => {
    window.speechSynthesis.cancel();

    const audio = audioRef.current;

    if (audio) {
      audio.pause();
      audio.currentTime = 0;
    }

    setIsSpeaking(false);
    setHasGenerated(false);
    setAudioUrl("");
    setText("");
  };

  // =========================
  // SHOW LOGIN
  // =========================
  if (!isLoggedIn) {
    return (
      <Login
        onLogin={() => setIsLoggedIn(true)}
      />
    );
  }

  // =========================
  // TTS APPLICATION
  // =========================
  return (
    <div className="app">
      <div className="container">

        {/* Header */}
        <header className="header">

          <div className="header-top">

            <div className="logo">
              🔊
            </div>

            <button
              className="logout-button"
              onClick={handleLogout}
            >
              🚪 Logout
            </button>

          </div>

          <h1>
            Text to Speech
          </h1>

          <p>
            Convert your text into natural-sounding speech
          </p>

        </header>

        <main className="card">

          {/* Text Section */}
          <section className="text-section">

            <label htmlFor="text">
              Enter your text
            </label>

            <textarea
              id="text"
              value={text}
              onChange={(e) =>
                setText(e.target.value)
              }
              maxLength={maxCharacters}
              placeholder="Type or paste your text here..."
            />

            <div className="text-info">

              <span>
                {characterCount} / {maxCharacters} characters
              </span>

              <span>
                {wordCount} words
              </span>

            </div>

          </section>

          {/* Language and Voice */}
          <section className="selectors">

            <div className="field">

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

                <option value="en-US">
                  English
                </option>

                <option value="hi-IN">
                  Hindi
                </option>

                <option value="gu-IN">
                  Gujarati
                </option>

                <option value="mr-IN">
                  Marathi
                </option>

                <option value="es-ES">
                  Spanish
                </option>

                <option value="fr-FR">
                  French
                </option>

                <option value="de-DE">
                  German
                </option>

              </select>

            </div>

            <div className="field">

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

                <option value="female">
                  Female Voice
                </option>

                <option value="male">
                  Male Voice
                </option>

              </select>

            </div>

          </section>

          {/* Main Buttons */}
          <div className="actions">

            <button
              className="generate-button"
              onClick={handleGenerate}
            >
              🔊 Generate Speech
            </button>

            <button
              className="clear-button"
              onClick={handleClear}
            >
              Clear
            </button>

          </div>

          {/* Generated Audio */}
          <section className="audio-section">

            <h2>
              🎵 Generated Audio
            </h2>

            {!hasGenerated && (
              <div className="audio-placeholder">

                <p>
                  Your generated audio will appear here.
                </p>

              </div>
            )}

            {hasGenerated && audioUrl && (
              <div className="generated-audio">

                <p>
                  ✅ Speech generated successfully.
                </p>

                <audio
                  ref={audioRef}
                  key={audioUrl}
                  src={audioUrl}
                  controls
                  preload="auto"

                  onPlay={() =>
                    setIsSpeaking(true)
                  }

                  onPause={() =>
                    setIsSpeaking(false)
                  }

                  onEnded={() =>
                    setIsSpeaking(false)
                  }

                  onError={(event) => {
                    console.error(
                      "Audio element error:",
                      event
                    );

                    setIsSpeaking(false);
                  }}
                />

                <div className="audio-controls">

                  <button
                    type="button"
                    className="download-button"
                    onClick={handlePlay}
                  >
                    ▶ Play Speech
                  </button>

                  <button
                    type="button"
                    className="download-button"
                    onClick={handleStop}
                  >
                    ⏹ Stop Speech
                  </button>

                  <a
                    className="download-button"
                    href={
                      audioUrl + "/download"
                    }
                    download
                  >
                    ⬇️ Download Audio
                  </a>

                </div>

                {isSpeaking && (
                  <p>
                    🔊 Audio is playing...
                  </p>
                )}

              </div>
            )}

          </section>

        </main>

        <footer>

          <p>
            Text-to-Speech Application
          </p>

        </footer>

      </div>
    </div>
  );
}

export default App;

