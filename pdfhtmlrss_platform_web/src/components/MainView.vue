<template>
  <div>
    <div style="
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content : space-between;
      height: 225px;
    ">
      <UserArea/>
      <OperationsSelector v-model="operation" @update:modelValue="dismissAlertMessage"/>
      <FileSelector :set-file="setFile"/>
      <form
          v-if="operation == Operation.SIGN_SELECT_REDACTABLE_ELEMS || operation == Operation.REDACT"
          style="display: flex; flex-direction: column; align-items: center; width: 500px"
      >
        <button style="width: 150px" :disabled="!file || documentViewOpened" @click.prevent="handleOpenDocumentView">Open document view</button>
        <div v-if="documentViewOpened">
          Paste the XPath URLs of the HTML elements you want to redact separated by lines. Hint: Use your browser DevTools.
          <textarea v-model="redactedElemsTextBoxRef" style="width: 300px; height: 100px"></textarea>
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
          :message="toastNotificationMessage"
          :type="toastNotificationType"
          :detailsClick="() => openSignatureVerifyReportWindow = true"
          :dismissClick="dismissAlertMessage"
      />
      <ToastNotification
          v-if="toastNotificationMessage && signatureVerificationReport == null"
          :message="toastNotificationMessage"
          :type="toastNotificationType"
          :dismissClick="dismissAlertMessage"
      />
      <button
          :disabled="isSubmitButtonDisabled()"
          @click="handleOperationButtonClick"
      >
        {{opToButtonTitle(operation)}}
      </button>
      <SignatureVerificationReportWindow
          v-if="openSignatureVerifyReportWindow && signatureVerificationReport"
          :closeWindow="() => openSignatureVerifyReportWindow = false"
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

import {defineEmits, ref} from 'vue'
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
import SignatureVerificationReport from "@/dto/SignatureVerificationReport";
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
// )c

const redactedElemsTextBoxRef = ref<String>()

async function setFile(newFile : File) {
  file.value = newFile;
  if(!redactionProcess.value)
    return
  try {
    await cancelRedactionProcess(redactionProcess.value?.taskId)
  } finally {
    redactionProcess.value = undefined
  }
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

  if(!redactionProcess.value)
    redactionProcess.value = await submitRedactionProcess(file.value, operation.value)

  let tmpHtmlFile = redactionProcess.value.tmpHtmlFile

  emit("open-document-view", getTemporaryFileURL(tmpHtmlFile));

  documentViewOpened.value = true

  // window.open(getTemporaryFileURL(tmpHtmlFile), "_blank")
  //     ?.focus()
}

function isSignatureValid(report : SignatureVerificationReport) : boolean {
    return report.padesNotModified && (!report.hasRSSSignature || report.rssNotModified);
}

function isSubmitButtonDisabled() {
  if(!file.value)
    return true;

  switch(operation.value) {
    case Operation.SIGN_SELECT_REDACTABLE_ELEMS:
    case Operation.REDACT:
      return (redactedElemsTextBoxRef?.value?.length ?? 0) == 0
    default:
      return false;
  }
}

function clearForm() {
  file.value = undefined
  redactionProcess.value = undefined
  redactedElemsTextBoxRef.value = ""
  documentViewOpened.value = false
  emit('close-document-view')
}

function dismissAlertMessage() {
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

  dismissAlertMessage()

  switch (operation.value) {
    case Operation.SIGN_SELECT_REDACTABLE_ELEMS:
    case Operation.REDACT:  {
      if(!redactionProcess.value)
        redactionProcess.value = await submitRedactionProcess(file.value, operation.value)

      let redactElems = redactedElemsTextBoxRef.value?.split("\n")
          ?.filter(e => e.startsWith("/html/body/"))
          ?.map(e => e.replace(/\/html\/body\/a\[.*]\//, "/html/body/"))
          ?.map(e => `#xpath(${e})`) ?? []

      await finishRedactionProcess(redactionProcess.value.taskId, redactElems)
          .then(res =>  {
            if(res.ok)
              return res?.blob()

            res.json()
                .then(j => showToastNotification(`Error: ${j?.message}`, ToastType.ERROR))
          })
          .then(blob => blob && downloadBlobRequest(blob))
          .finally(() => clearForm())
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
