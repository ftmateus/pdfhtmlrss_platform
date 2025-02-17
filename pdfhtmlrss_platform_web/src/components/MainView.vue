<template>
  <div>
    <div style="
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content : space-around;
      gap: 0.8rem;
      height: 100%;
    ">
      <UserArea @logout="() => documentViewOpened = false"/>
      <OperationsSelector v-model="operation" @update:modelValue="dismissToastNotification"/>
      <FileSelector :set-file="setFile"/>
      <form
          v-if="operation == Operation.SIGN_SELECT_REDACTABLE_ELEMS || operation == Operation.REDACT"
          style="display: flex; flex-direction: column; align-items: center; width: 500px"
      >
        <button style="width: 150px" :disabled="!file || documentViewOpened" @click.prevent="handleOpenDocumentView">Open document view</button>
        <div v-if="documentViewOpened">
            Select the parts of the document to be redacted.
        </div>
<!--        <fieldset v-if="operation == Operation.SIGN_SELECT_REDACTABLE_ELEMS">-->
<!--          <legend>Signature Options</legend>-->
<!--          <input type="radio" name="sign_option" id="improved_compatibility" checked>-->
<!--          <label for="improved_compatibility">Improved compatibility</label>-->
<!--          <input type="radio" name="sign_option" id="smaller_size">-->
<!--          <label for="smaller_size">Smaller file</label>-->
<!--        </fieldset>-->
      </form>
      <ToastNotification
          v-if="toastNotificationMessage && signatureVerificationReport != null"
          :type="toastNotificationType"
          :detailsClick="() => openSignatureVerifyReportWindow = true"
          :dismissClick="dismissSignatureVerificationToastNotification"
      >
        {{ toastNotificationMessage }}
      </ToastNotification>
      <ToastNotification
          v-if="toastNotificationMessage && signatureVerificationReport == null"
          :type="toastNotificationType"
          :dismissClick="dismissToastNotification"
      >
        {{ toastNotificationMessage }}
      </ToastNotification>
      <button
          :disabled="isSubmitButtonDisabled()"
          @click="handleOperationButtonClick"
      >
        {{ opToButtonTitle(operation) }}
      </button>
      <SignatureVerificationReportWindow
          v-if="openSignatureVerifyReportWindow && signatureVerificationReport"
          @close-window="() => openSignatureVerifyReportWindow = false"
          :report="signatureVerificationReport"
      />
<!--      <OperationButton-->
<!--          :operation="operation"-->
<!--          @click="handleOperationButtonClick"-->
<!--          :no-file-selected="isSubmitButtonDisabled()"-->
<!--      />-->
    </div>
  </div>
</template>

<script setup lang="ts">

import { defineEmits, ref, watch } from 'vue'
import FileSelector from "@/components/FileSelector.vue";
import OperationsSelector from "@/components/OperationsSelector.vue";
// import OperationButton from "@/components/OperationButton.vue";
import {Operation, opToButtonTitle} from "@/components/Operations";
import UserArea from "@/components/UserArea.vue";
import {
  cancelRedactionProcess,
  finishRedactionProcess,
  getTemporaryFileURL,
  signOnly,
  submitRedactionProcess,
  verifyDocument
} from "@/api";
import {RedactionProcess} from "@/dto/RedactionProcess";
import SignatureVerificationReport, {isSignatureValid} from "@/dto/SignatureVerificationReport";
import SignatureVerificationReportWindow from "@/components/SignatureVerificationReportWindow.vue";
import ToastNotification from "@/components/ToastNotification.vue";
import {ToastType} from "@/components/ToastNotificationType";
// import { RefSymbol } from '@vue/reactivity';



const file = ref<File>();
const operation = ref(Operation.SIGN_SELECT_REDACTABLE_ELEMS)
const toastNotificationMessage = ref<String | undefined>()
const toastNotificationType = ref<ToastType | undefined>(undefined)
const redactionProcess = ref<RedactionProcess>();
const openSignatureVerifyReportWindow = ref<Boolean>(false);
const signatureVerificationReport = ref<SignatureVerificationReport | undefined>(undefined);
const documentViewOpened = ref(false);

const emit = defineEmits(['open-document-view', 'close-document-view'])
// const redactableSignatureOption = ref<RedactableSignatureOption>(
//     RedactableSignatureOption.IMPROVED_COMPATIBILITY
// )

watch(documentViewOpened, (newValue) => {
    if(newValue == true) {
      if(!redactionProcess.value)
        return

      let tmpHtmlFile = redactionProcess.value.tmpHtmlFile

      emit("open-document-view", getTemporaryFileURL(tmpHtmlFile));
    }
    else {
      emit('close-document-view')
    }
})


async function setFile(newFile : File) {
  file.value = newFile;
  documentViewOpened.value = false

  if(!redactionProcess.value)
    return
  try {
    await cancelRedactionProcess(redactionProcess.value?.taskId)
  } finally {
    redactionProcess.value = undefined
  }
}

function fileSupported() : boolean {
  if(!file.value)
    return false

  let fileType =  file.value.type

  return fileType == "application/pdf" || fileType == "text/html"
}

function downloadBlobRequest(blob : Blob) {
  let fileUrl = window.URL.createObjectURL(blob);
  let a = document.createElement('a');
  a.href = fileUrl;
  a.download = file.value?.name ?? "";
  document.body.appendChild(a); // we need to append the element to the dom -> otherwise it will not work in firefox
  a.click();
  a.remove();
}

async function handleOpenDocumentView() {
  if(!file.value)
    return;

  if(operation.value != Operation.SIGN_SELECT_REDACTABLE_ELEMS
  && operation.value != Operation.REDACT)
    return;

  if(!fileSupported()) {
    showToastNotification("File not supported!", ToastType.ERROR);
    return;
  }

  try {
    if(!redactionProcess.value)
      redactionProcess.value = await submitRedactionProcess(file.value, operation.value)
    documentViewOpened.value = true
  } catch(e : any) {
    showToastNotification(`Error: ${e.message}`, ToastType.ERROR);
  }

}

function isSubmitButtonDisabled() {
  if(!file.value)
    return true;

  switch(operation.value) {
    case Operation.SIGN_SELECT_REDACTABLE_ELEMS:
    case Operation.REDACT:
      return !documentViewOpened.value
    default:
      return false;
  }
}

function clearForm() {
  file.value = undefined
  redactionProcess.value = undefined
  documentViewOpened.value = false
}

function dismissSignatureVerificationToastNotification() {
  signatureVerificationReport.value = undefined
  dismissToastNotification()
}

function dismissToastNotification() {
  toastNotificationMessage.value = ""
  toastNotificationType.value = undefined
}

function showToastNotification(message : String, toastType : ToastType) {
  toastNotificationMessage.value = message
  toastNotificationType.value = toastType
}

async function handleOperationButtonClick() {
  if(!file.value)
    return

  dismissToastNotification()

  switch (operation.value) {
    case Operation.SIGN_SELECT_REDACTABLE_ELEMS:
    case Operation.REDACT:  {
      if(!redactionProcess.value)
        redactionProcess.value = await submitRedactionProcess(file.value, operation.value)

      let elementsToRedact : string[] = JSON.parse(localStorage.getItem("elementsToRedact") ?? "[]")
          .map((e : string) => `#xpath(${e})`)

      if(elementsToRedact.length == 0) {
        showToastNotification("No document elements have been selected!", ToastType.ERROR)
        return;
      }

      await finishRedactionProcess(redactionProcess.value.taskId, elementsToRedact)
          .then(res =>  {
            if(!res.ok)
              res.json()
              .then(j => showToastNotification(`Error: ${j?.message}`, ToastType.ERROR))

            return res?.blob()
          })
          .then(blob => {
            if(!blob)
              return
            downloadBlobRequest(blob)
            toastNotificationMessage.value = "Document was sucessfully signed!"
            toastNotificationType.value = ToastType.SUCCESS
            clearForm()
          })
      break;
    }
    case Operation.SIGN_ONLY : {
      await signOnly(file.value!)
          .then(blob => blob && downloadBlobRequest(blob))
          .catch(e => showToastNotification(`Error: ${e.message}`, ToastType.ERROR))
          .finally(() => clearForm())
      break;
    }
    case Operation.VERIFY : {
      await verifyDocument(file.value!)
          .then(report => {
              if(!report.isSigned)
                showToastNotification("Document is not signed!", ToastType.ERROR)
              else if(isSignatureValid(report))
                showToastNotification("Document has valid signature!", ToastType.SUCCESS)
              else
                showToastNotification("Document has invalid signature!", ToastType.ERROR)

              signatureVerificationReport.value = report.isSigned ? report : undefined;
          })
          .catch(e => showToastNotification("Error: " + e.message, ToastType.ERROR))
    }
  }
}

</script>
<style scoped>
h3 {
  margin: 40px 0 0;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}

.modal-darken-background {
  background: rgba(0, 0, 0, 0.5);
}

</style>
