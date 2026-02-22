<script setup lang="ts">
import { ref, onMounted } from 'vue'
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

async function loadAudio() {
  try {
    audioData.value = await store.getAudioUrl(props.interviewId)
  } catch {
    // error captured in store
  }
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
    emit('audioChanged')
  } catch {
    // error captured in store
  }
}
</script>

<template>
  <div class="audio-section">
    <h3 class="audio-title">Audio Recording</h3>

    <div v-if="uploadError" class="audio-error" role="alert">
      {{ uploadError }}
      <button class="error-dismiss" @click="uploadError = null">×</button>
    </div>

    <!-- Upload state -->
    <div v-if="!audioData" class="audio-upload">
      <input
        ref="fileInput"
        type="file"
        accept=".mp3,.wav,.m4a,.flac,.ogg,.webm"
        class="file-input-hidden"
        data-testid="audio-file-input"
        @change="handleFileSelect"
      />
      <div class="upload-area" @click="triggerUpload">
        <span class="upload-icon">🎤</span>
        <p class="upload-text">
          {{ uploading ? 'Uploading...' : 'Click to upload audio recording' }}
        </p>
        <p class="upload-hint">MP3, WAV, M4A, FLAC, OGG, WebM — max {{ MAX_SIZE_MB }} MB</p>
      </div>
    </div>

    <!-- Audio player state -->
    <div v-else class="audio-player">
      <audio controls :src="audioData.audioUrl" class="audio-element">
        Your browser does not support the audio element.
      </audio>

      <div v-if="audioData.audioExpiresAt" class="audio-meta">
        <span class="meta-label">Expires:</span>
        {{ new Date(audioData.audioExpiresAt).toLocaleDateString() }}
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
</style>
