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
      <FileSelector :set-file="setFile">
        <p>Drag file to this <i>drop zone</i>.<span style="color: red">*</span></p>
      </FileSelector>
      <div v-if="operation == Operation.VERIFY">
        <input name="derivation" type="checkbox" v-model="verifyFileDerivation"/>
        <label for="derivation">Check derivation from original file.</label>
      </div>
      <FileSelector :set-file="setOriginalFileDerivation" style="background-color: lightsteelblue;" v-if="operation == Operation.VERIFY && verifyFileDerivation">
        <p>Drop original document to verify if redacted document is derived from it.</p>
      </FileSelector>
      <form
          v-if="operation == Operation.SIGN_SELECT_REDACTABLE_ELEMS || operation == Operation.REDACT"
          style="display: flex; flex-direction: column; align-items: center; width: 500px"
      >
        <button :disabled="!mainFile || documentViewOpened" @click.prevent="handleOpenDocumentView">
          <i class="pi pi-external-link" style="font-size: 0.8rem"></i>
          Open document view
        </button>
        <div v-if="documentViewOpened && operation == Operation.SIGN_SELECT_REDACTABLE_ELEMS">
            Select the redactable parts of the document by double clicking.
        </div>
        <div v-if="documentViewOpened && operation == Operation.REDACT">
            Redact the document by double clicking.
            Only the elements with <label style="border: 1px solid orange;">orange border</label> are allowed.
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
      <ModalWindow
          v-if="openSignatureVerifyReportWindow && signatureVerificationReport"
          @close-window="() => openSignatureVerifyReportWindow = false"
      >
          <SignatureVerificationReportView
              title="Signature Details"
              :report="signatureVerificationReport"
          />
      </ModalWindow>
      <ModalWindow
          v-if="openSignatureVerifyReportWindow && signatureVerificationReport && verifyFileDerivation"
          @close-window="() => openSignatureVerifyReportWindow = false"
      >
        <h2>Signature Details</h2>
        <div style="display: flex; flex-direction: row; flex-wrap: wrap; gap: 1rem" >
          <SignatureVerificationReportView
              title="Original Document"
              :report="signatureVerificationReport"
          />
          <SignatureVerificationReportView
              title="Redacted Document"
              :report="signatureVerificationReport"
          />
        </div>
      </ModalWindow>
<!--      <OperationButton-->
<!--          :operation="operation"-->
<!--          @click="handleOperationButtonClick"-->
<!--          :no-file-selected="isSubmitButtonDisabled()"-->
<!--      />-->
    </div>
  </div>
</template>

<script setup lang="ts">

import {defineEmits, ref, watch} from 'vue'
import FileSelector from "@/components/FileSelector.vue";
import OperationsSelector from "@/components/OperationsSelector.vue";
// import OperationButton from "@/components/OperationButton.vue";
import {Operation, opToButtonTitle} from "@/components/Operations";
import UserArea from "@/components/UserArea.vue";
import {
  cancelRedactionProcess,
  finishRedactionProcess, getRSSSignatureDebug,
  getTemporaryFileURL,
  signOnly,
  submitRedactionProcess,
  verifyDocument
} from "@/api";
import {RedactionProcess} from "@/dto/RedactionProcess";
import SignatureVerificationReport, {isSignatureValid} from "@/dto/SignatureVerificationReport";
import SignatureVerificationReportView from "@/components/SignatureVerificationReportView.vue";
import ToastNotification from "@/components/ToastNotification.vue";
import {ToastType} from "@/components/ToastNotificationType";
import ModalWindow from "@/components/ModalWindow.vue";
// import { RefSymbol } from '@vue/reactivity';



const mainFile = ref<File>();
const verifyFileDerivation = ref<Boolean>();
const originalFileDerivation = ref<File>();
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
  mainFile.value = newFile;
  documentViewOpened.value = false

  if(!redactionProcess.value)
    return
  try {
    await cancelRedactionProcess(redactionProcess.value?.taskId)
  } finally {
    redactionProcess.value = undefined
  }
}

async function setOriginalFileDerivation(newFile : File) {
  originalFileDerivation.value = newFile;
  documentViewOpened.value = false

  if(!redactionProcess.value)
    return
  try {
    await cancelRedactionProcess(redactionProcess.value?.taskId)
  } finally {
    redactionProcess.value = undefined
  }
}

function checkFile() : boolean {
  if(!mainFile.value)
    return false;

  let fileType =  mainFile.value.type

  if(fileType != "application/pdf" && fileType != "text/html") {
    showToastNotification("File type not supported!", ToastType.ERROR);
    return false;
  }

  if(mainFile.value.size > 5 * 1024 * 1024) {
    showToastNotification("File is too big!", ToastType.ERROR);
    return false;
  }

  return true;
}

function downloadBlobRequest(blob : Blob, filename? : string) {
  let fileUrl = window.URL.createObjectURL(blob);
  let a = document.createElement('a');
  a.href = fileUrl;
  a.download = filename ?? (mainFile.value?.name ?? "");
  document.body.appendChild(a); // we need to append the element to the dom -> otherwise it will not work in firefox
  a.click();
  a.remove();
}

async function handleOpenDocumentView() {
  if(!mainFile.value || !checkFile())
    return;

  if(operation.value != Operation.SIGN_SELECT_REDACTABLE_ELEMS
  && operation.value != Operation.REDACT)
    return;

  showToastNotification("Generating and opening document view...", ToastType.INFO);


  try {
    if(!redactionProcess.value)
      redactionProcess.value = await submitRedactionProcess(mainFile.value, operation.value)
    documentViewOpened.value = true
    dismissToastNotification()
  } catch(e : any) {
    showToastNotification(`Error: ${e.message}`, ToastType.ERROR);
  }

}

function isSubmitButtonDisabled() {
  if(!mainFile.value)
    return true;

  switch(operation.value) {
    case Operation.SIGN_SELECT_REDACTABLE_ELEMS:
    case Operation.REDACT:
      return !documentViewOpened.value
    case Operation.VERIFY:
        return verifyFileDerivation.value && !originalFileDerivation.value;
    default:
      return false;
  }
}

function clearForm() {
  mainFile.value = undefined
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

async function handleRedactOperation() {
  if(!redactionProcess.value)
    return

  let elementsToRedact : string[] = JSON.parse(localStorage.getItem("elementsToRedact") ?? "[]")
      .map((e : string) => `#xpath(${e})`)

  if(elementsToRedact.length == 0) {
    showToastNotification("No document elements have been selected!", ToastType.ERROR)
    return;
  }

  if(operation.value == Operation.REDACT)
    showToastNotification("Redacting document...", ToastType.INFO)
  else
    showToastNotification("Signing document...", ToastType.INFO)

  try {
    let blob = await finishRedactionProcess(redactionProcess.value.taskId, elementsToRedact)
    downloadBlobRequest(blob)
    if(operation.value == Operation.REDACT)
      showToastNotification("Document was successfully redacted!", ToastType.SUCCESS)
    else
      showToastNotification("Document was successfully signed!", ToastType.SUCCESS)
    clearForm()
  } catch (e : any) {
    showToastNotification(`Error: ${e?.message}`, ToastType.ERROR)
  }
}

async function handleVerifyDocument() {
  showToastNotification("Verifying document...", ToastType.INFO)
  try {
    let report = await verifyDocument(mainFile.value!)
    if(!report.isSigned)
      showToastNotification("Document is not signed!", ToastType.ERROR)
    else if(isSignatureValid(report))
      showToastNotification("Document has valid signature!", ToastType.SUCCESS)
    else
      showToastNotification("Document has invalid signature!", ToastType.ERROR)

    signatureVerificationReport.value = report.isSigned ? report : undefined;
  }
  catch(e : any) {
    showToastNotification("Error: " + e.message, ToastType.ERROR)
  }
}

async function handleSignDocument() {
  showToastNotification("Signing document...", ToastType.INFO)
  try {
    let blob = await signOnly(mainFile.value!)
    showToastNotification("Document was successfully signed!", ToastType.SUCCESS)
    downloadBlobRequest(blob)
  } catch (e : any) {
      showToastNotification(`Error: ${e.message}`, ToastType.ERROR)
  }
  finally {
    clearForm()
  }
}

async function handleOperationButtonClick() {
  if(!mainFile.value || !checkFile())
    return;

  dismissToastNotification()

  switch (operation.value) {
    case Operation.SIGN_SELECT_REDACTABLE_ELEMS:
    case Operation.REDACT:  {
      await handleRedactOperation()
      break;
    }
    case Operation.SIGN_ONLY : {
      await handleSignDocument()
      break;
    }
    case Operation.VERIFY : {
      await handleVerifyDocument()
      break;
    }
    case Operation.GET_RSS_SIG : {
      await getRSSSignatureDebug(mainFile.value)
          .then(res =>  {
            if(!res.ok)
            {
              res.json()
                  .then(j => showToastNotification(`Error: ${j?.message}`, ToastType.ERROR))
              return
            }

            return res?.blob()
          })
          .then(blob => {
            if(!blob)
              return

            downloadBlobRequest(blob, mainFile.value?.name + ".rss.xml")
            toastNotificationMessage.value = "RSS signature was downloaded!"
            toastNotificationType.value = ToastType.SUCCESS
            clearForm()
          })
      break;
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
