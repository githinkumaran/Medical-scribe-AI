from flask import Flask, request, jsonify
import whisper
import os
import tempfile

app = Flask(__name__)

print("Loading Whisper model...")
model = whisper.load_model("base")
print("Whisper model loaded successfully!")

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "Whisper service running!"})

@app.route('/transcribe', methods=['POST'])
def transcribe():
    try:
        if 'audio' not in request.files:
            return jsonify({"error": "No audio file provided"}), 400

        audio_file = request.files['audio']
        print(f"Received file: {audio_file.filename}, content type: {audio_file.content_type}")

        # Save with original extension
        suffix = '.webm'
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
            audio_file.save(tmp.name)
            tmp_path = tmp.name
            print(f"Saved to: {tmp_path}")

        # Try ffmpeg conversion first
        wav_path = tmp_path.replace(suffix, '.wav')
        converted = False
        
        try:
            import subprocess
            result = subprocess.run([
                'ffmpeg', '-y', '-i', tmp_path,
                '-ar', '16000', '-ac', '1',
                '-f', 'wav', wav_path
            ], capture_output=True, text=True, timeout=30)
            
            if result.returncode == 0 and os.path.exists(wav_path):
                converted = True
                print("FFmpeg conversion successful")
            else:
                print(f"FFmpeg error: {result.stderr}")
        except FileNotFoundError:
            print("FFmpeg not found, trying direct transcription")
        except Exception as ex:
            print(f"FFmpeg exception: {ex}")

        # Transcribe
        transcribe_path = wav_path if converted else tmp_path
        print(f"Transcribing: {transcribe_path}")
        
        result = model.transcribe(transcribe_path)
        transcript = result["text"].strip()
        print(f"Transcript: {transcript}")

        # Cleanup
        try:
            os.unlink(tmp_path)
            if converted and os.path.exists(wav_path):
                os.unlink(wav_path)
        except Exception:
            pass

        return jsonify({"transcript": transcript, "status": "success"})

    except Exception as e:
        import traceback
        print(f"ERROR: {traceback.format_exc()}")
        return jsonify({"error": str(e), "status": "failed"}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)