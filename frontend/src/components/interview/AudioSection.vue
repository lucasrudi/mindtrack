<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useInterviewsStore, type AudioResponse } from '@/stores/interviews'

const props = defineProps<{
  interviewId: number
  hasAudio: boolean
}>()

const emit = defineEmits<{
  audioChanged: []
}>()

const store = useInterviewsStore()
const audioData = ref<AudioResponse | null>(null)
const uploading = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const uploadError = ref<string | null>(null)

type RecordState = 'idle' | 'recording' | 'uploading' | 'transcribing' | 'done' | 'failed'
const recordState = ref<RecordState>('idle')
const recordDuration = ref(0)
let mediaRecorder: MediaRecorder | null = null
let durationInterval: ReturnType<typeof setInterval> | null = null
let pollCount = 0

const ALLOWED_FORMATS = [
  'audio/mpeg',
  'audio/wav',
  'audio/x-m4a',
  'audio/flac',
  'audio/ogg',
  'audio/webm',
]
const MAX_SIZE_MB = 50
const MAX_SIZE_BYTES = MAX_SIZE_MB * 1024 * 1024

onMounted(async () => {
  if (props.hasAudio) {
    await loadAudio()
  }
})

onUnmounted(() => {
  if (durationInterval) clearInterval(durationInterval)
  mediaRecorder?.stop()
})

async function loadAudio() {
  try {
    audioData.value = await store.getAudioUrl(props.interviewId)
    if (audioData.value?.transcriptionStatus === 'IN_PROGRESS') {
      scheduleTranscriptionPoll()
    }
  } catch {
    // error captured in store
  }
}

async function startRecording() {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    const chunks: BlobPart[] = []
    mediaRecorder = new MediaRecorder(stream, { mimeType: 'audio/webm' })
    mediaRecorder.ondataavailable = (e) => chunks.push(e.data)
    mediaRecorder.onstop = async () => {
      stream.getTracks().forEach((t) => t.stop())
      const blob = new Blob(chunks, { type: 'audio/webm' })
      await uploadBlob(blob)
    }
    mediaRecorder.start()
    recordState.value = 'recording'
    recordDuration.value = 0
    durationInterval = setInterval(() => recordDuration.value++, 1000)
  } catch {
    uploadError.value = 'Microphone access denied.'
  }
}

function stopRecording() {
  if (durationInterval) {
    clearInterval(durationInterval)
    durationInterval = null
  }
  mediaRecorder?.stop()
  recordState.value = 'uploading'
}

async function uploadBlob(blob: Blob) {
  const file = new File([blob], 'recording.webm', { type: 'audio/webm' })
  try {
    audioData.value = await store.uploadAudio(props.interviewId, file)
    emit('audioChanged')
    recordState.value = 'transcribing'
    pollCount = 0
    setTimeout(() => pollTranscription(), 20000)
  } catch {
    uploadError.value = 'Upload failed. Please try again.'
    recordState.value = 'failed'
  }
}

async function pollTranscription() {
  try {
    const data = await store.getAudioUrl(props.interviewId)
    audioData.value = data
    if (data?.transcriptionStatus === 'COMPLETED' || data?.transcriptionStatus === 'FAILED') {
      recordState.value = data.transcriptionStatus === 'COMPLETED' ? 'done' : 'failed'
      return
    }
  } catch {
    // ignore poll errors
  }
  pollCount++
  if (pollCount < 4) {
    setTimeout(() => pollTranscription(), 10000)
  } else {
    uploadError.value = 'Transcription taking longer than expected — refresh later.'
    recordState.value = 'done'
  }
}

function scheduleTranscriptionPoll() {
  pollCount = 0
  setTimeout(() => pollTranscription(), 10000)
}

function triggerUpload() {
  fileInput.value?.click()
}

async function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  uploadError.value = null

  if (!ALLOWED_FORMATS.includes(file.type)) {
    uploadError.value = 'Unsupported format. Use MP3, WAV, M4A, FLAC, OGG, or WebM.'
    return
  }

  if (file.size > MAX_SIZE_BYTES) {
    uploadError.value = `File too large. Maximum size is ${MAX_SIZE_MB} MB.`
    return
  }

  uploading.value = true
  try {
    audioData.value = await store.uploadAudio(props.interviewId, file)
    emit('audioChanged')
    scheduleTranscriptionPoll()
  } catch {
    uploadError.value = 'Upload failed. Please try again.'
  } finally {
    uploading.value = false
    if (fileInput.value) {
      fileInput.value.value = ''
    }
  }
}

async function handleDelete() {
  try {
    await store.deleteAudio(props.interviewId)
    audioData.value = null
    recordState.value = 'idle'
    emit('audioChanged')
  } catch {
    // error captured in store
  }
}

function formatDuration(seconds: number): string {
  const m = Math.floor(seconds / 60)
    .toString()
    .padStart(2, '0')
  const s = (seconds % 60).toString().padStart(2, '0')
  return `${m}:${s}`
}
</script>

<template>
  <div class="audio-section">
    <h3 class="audio-title">Audio Recording</h3>

    <div v-if="uploadError" class="audio-error" role="alert">
      {{ uploadError }}
      <button class="error-dismiss" @click="uploadError = null">×</button>
    </div>

    <!-- No audio yet: record or upload -->
    <div v-if="!audioData && recordState === 'idle'" class="audio-upload">
      <input
        ref="fileInput"
        type="file"
        accept=".mp3,.wav,.m4a,.flac,.ogg,.webm"
        class="file-input-hidden"
        data-testid="audio-file-input"
        @change="handleFileSelect"
      />
      <button class="btn btn-record" @click="startRecording">🎙 Start Recording</button>
      <div class="upload-area" @click="triggerUpload">
        <span class="upload-icon">📁</span>
        <p class="upload-text">Or click to upload a file</p>
        <p class="upload-hint">MP3, WAV, M4A, FLAC, OGG, WebM — max {{ MAX_SIZE_MB }} MB</p>
      </div>
    </div>

    <!-- Recording in progress -->
    <div v-if="recordState === 'recording'" class="record-active">
      <span class="record-dot" />
      <span class="record-timer">{{ formatDuration(recordDuration) }}</span>
      <button class="btn btn-stop" @click="stopRecording">⏹ Stop</button>
    </div>

    <!-- Uploading / transcribing -->
    <div v-if="recordState === 'uploading' || recordState === 'transcribing'" class="status-msg">
      <span v-if="recordState === 'uploading'">Uploading...</span>
      <span v-else>Transcribing… this may take up to a minute.</span>
    </div>

    <!-- Failed -->
    <div v-if="recordState === 'failed'" class="status-msg error">
      Transcription failed. The audio was saved — you can refresh to check again.
    </div>

    <!-- Audio player state -->
    <div v-if="audioData" class="audio-player">
      <audio controls :src="audioData.audioUrl" class="audio-element">
        Your browser does not support the audio element.
      </audio>

      <div v-if="audioData.audioExpiresAt" class="audio-meta">
        <span class="meta-label">Expires:</span>
        {{ new Date(audioData.audioExpiresAt).toLocaleDateString() }}
      </div>

      <div
        v-if="audioData.transcriptionStatus === 'IN_PROGRESS' || recordState === 'transcribing'"
        class="transcription-pending"
      >
        Transcribing audio...
      </div>

      <div v-if="audioData.transcriptionText" class="transcription">
        <h4 class="transcription-title">Transcription</h4>
        <p class="transcription-text">{{ audioData.transcriptionText }}</p>
      </div>

      <div class="audio-actions">
        <button class="btn btn-upload" :disabled="uploading" @click="triggerUpload">
          {{ uploading ? 'Uploading...' : 'Replace Audio' }}
        </button>
        <input
          ref="fileInput"
          type="file"
          accept=".mp3,.wav,.m4a,.flac,.ogg,.webm"
          class="file-input-hidden"
          data-testid="audio-file-input"
          @change="handleFileSelect"
        />
        <button class="btn btn-delete" @click="handleDelete">Delete Audio</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.audio-section {
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg, 12px);
  padding: var(--space-5);
  background: var(--color-gray-50, #f9fafb);
}

.audio-title {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold, 600);
  color: var(--color-gray-800, #1f2937);
  margin-bottom: var(--space-4);
}

.audio-error {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: var(--color-error);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--border-radius);
  margin-bottom: var(--space-4);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--font-size-sm);
}

.error-dismiss {
  background: none;
  border: none;
  color: var(--color-error);
  font-size: var(--font-size-lg);
  cursor: pointer;
  padding: 0 var(--space-1);
}

.file-input-hidden {
  display: none;
}

.upload-area {
  border: 2px dashed var(--color-gray-300);
  border-radius: var(--border-radius);
  padding: var(--space-6);
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.upload-area:hover {
  border-color: var(--color-primary);
  background: var(--color-primary-50, #eff6ff);
}

.upload-icon {
  font-size: 2rem;
  display: block;
  margin-bottom: var(--space-2);
}

.upload-text {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-700);
  margin-bottom: var(--space-1);
}

.upload-hint {
  font-size: var(--font-size-xs, 0.75rem);
  color: var(--color-gray-500);
}

.audio-element {
  width: 100%;
  margin-bottom: var(--space-3);
}

.audio-meta {
  font-size: var(--font-size-sm);
  color: var(--color-gray-500);
  margin-bottom: var(--space-3);
}

.meta-label {
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-600);
}

.transcription-pending {
  font-size: var(--font-size-sm);
  color: var(--color-gray-500);
  font-style: italic;
  margin-bottom: var(--space-3);
}

.transcription {
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius);
  padding: var(--space-4);
  margin-bottom: var(--space-4);
}

.transcription-title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold, 600);
  color: var(--color-gray-700);
  margin-bottom: var(--space-2);
}

.transcription-text {
  font-size: var(--font-size-sm);
  color: var(--color-gray-600);
  line-height: 1.6;
  white-space: pre-wrap;
}

.audio-actions {
  display: flex;
  gap: var(--space-3);
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-2) var(--space-4);
  border-radius: var(--border-radius);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-sm);
  cursor: pointer;
  border: none;
  transition: all var(--transition-fast);
}

.btn-record {
  display: block;
  width: 100%;
  margin-bottom: var(--space-3);
  padding: var(--space-3);
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--border-radius);
  font-weight: 600;
  cursor: pointer;
}

.btn-upload {
  background: var(--color-gray-100);
  color: var(--color-gray-700);
}

.btn-upload:hover:not(:disabled) {
  background: var(--color-gray-200);
}

.btn-upload:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-delete {
  background: #fef2f2;
  color: var(--color-error);
}

.btn-delete:hover {
  background: #fecaca;
}

.record-active {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
}

.record-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: red;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}

.record-timer {
  font-size: 1.25rem;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.btn-stop {
  margin-left: auto;
  padding: var(--space-2) var(--space-4);
  background: #fee2e2;
  color: var(--color-error);
  border: none;
  border-radius: var(--border-radius);
  font-weight: 600;
  cursor: pointer;
}

.status-msg {
  padding: var(--space-4);
  text-align: center;
  color: var(--color-gray-600);
}

.status-msg.error {
  color: var(--color-error);
}
</style>
