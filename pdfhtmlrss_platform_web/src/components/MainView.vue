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
      <OperationsSelector v-model="operation"/>
      <FileSelector :set-file="setFile"/>
      <form
          v-if="operation == Operation.SIGN_SELECT_REDACTABLE_ELEMS || operation == Operation.REDACT"
          style="display: flex; flex-direction: column; align-items: center; width: 500px"
      >
        <button style="width: 150px" :disabled="!file" @click.prevent="handleOpenDocumentView">Open document view</button>
        Paste the XPath URLs of the HTML elements you want to redact separated by lines. Hint: Use your browser DevTools.
        <textarea v-model="redactedElemsTextBoxRef" style="width: 300px; height: 100px"></textarea>
<!--        <fieldset v-if="operation == Operation.SIGN_SELECT_REDACTABLE_ELEMS">-->
<!--          <legend>Signature Options</legend>-->
<!--          <input type="radio" name="sign_option" id="improved_compatibility" checked>-->
<!--          <label for="improved_compatibility">Improved compatibility</label>-->
<!--          <input type="radio" name="sign_option" id="smaller_size">-->
<!--          <label for="smaller_size">Smaller file</label>-->
<!--        </fieldset>-->
      </form>
      <label v-if="alertMessage"
             :style="{ color : alertType ? 'red' : 'black'}">
        {{ alertMessage }}
        <a href="#" v-if="signatureVerificationReport != null" @click="() => openWindow = true">Details</a>
      </label>
      <button
          :disabled="isSubmitButtonDisabled()"
          @click="handleOperationButtonClick"
      >
        {{opToButtonTitle(operation)}}
      </button>
      <SignatureVerificationReportWindow
          v-if="openWindow && signatureVerificationReport"
          :closeWindow="() => openWindow = false"
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
import {ref} from 'vue'
import FileSelector from "@/components/FileSelector.vue";
import OperationsSelector from "@/components/OperationsSelector.vue";
// import OperationButton from "@/components/OperationButton.vue";
import {Operation, opToButtonTitle} from "@/components/Operations";
import UserArea from "@/components/UserArea.vue";
import {
  cancelRedactionProcess, finishRedactionProcess,
  getTemporaryFileURL,
  // RedactableSignatureOption,
  signOnly,
  submitRedactionProcess, verifyDocument
} from "@/api";
import {RedactionProcess} from "@/dto/RedactionProcess";
import SignatureVerificationReport from "@/dto/SignatureVerificationReport";
import SignatureVerificationReportWindow from "@/components/SignatureVerificationReportWindow.vue";
// import { RefSymbol } from '@vue/reactivity';



const file = ref<File>();
const operation = ref(Operation.SIGN_SELECT_REDACTABLE_ELEMS)
const alertMessage = ref<String | undefined>()
const alertType = ref<Boolean>(false)
const redactionProcess = ref<RedactionProcess>();
const openWindow = ref<Boolean>(false);
const signatureVerificationReport = ref<SignatureVerificationReport | undefined>(undefined);
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

  window.open(getTemporaryFileURL(tmpHtmlFile), "_blank")
      ?.focus()
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
}

function handleAlertMessage(message : String, error : Boolean) {
  alertMessage.value = message
  alertType.value = error
}

async function handleOperationButtonClick() {
  if(!file.value)
    return

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
                .then(j => handleAlertMessage(`Error: ${j?.message}`, true))
          })
          .then(blob => blob && downloadBlobRequest(blob))
          .then(() => clearForm())
      break;
    }
    case Operation.SIGN_ONLY : {
      await signOnly(file.value!)
          .then(res => res.blob())
          .then(blob => downloadBlobRequest(blob))
      break;
    }
    case Operation.VERIFY : {
      await verifyDocument(file.value!)
          .then(report => {
              signatureVerificationReport.value = report
              if(report.padesNotModified && (!report.hasRSSSignature || report.rssNotModified))
                handleAlertMessage("Document has valid signature!", false)
              else
                handleAlertMessage("Document was not signed or has invalid signature!", true)
          })
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
a {
  color: #42b983;
}

.modal-darken-background {
  background: rgba(0, 0, 0, 0.5);
}

</style>
