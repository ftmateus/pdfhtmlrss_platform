<template>
  <div class="hello">
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
        Paste the XPath elements URLs you want to redact separated by lines. Hint: Use your browser DevTools.
        <textarea v-model="redactedElemsTextBoxRef" style="width: 300px; height: 100px"></textarea>
        <div v-if="operation == Operation.SIGN_SELECT_REDACTABLE_ELEMS">
          <input type="radio" name="sign_option" id="compatibility" checked>
          <label for="compatibility">Improved compatibility</label>
          <input type="radio" name="sign_option" id="size">
          <label for="size">Smaller file</label>
        </div>
      </form>
      <button
          :disabled="isSubmitButtonDisabled()"
          @click="handleOperationButtonClick"
      >
        {{opToButtonTitle(operation)}}
      </button>
<!--      <OperationButton-->
<!--          :operation="operation"-->
<!--          @click="handleOperationButtonClick"-->
<!--          :no-file-selected="isSubmitButtonDisabled()"-->
<!--      />-->
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import FileSelector from "@/components/FileSelector.vue";
import OperationsSelector from "@/components/OperationsSelector.vue";
// import OperationButton from "@/components/OperationButton.vue";
import {Operation, opToButtonTitle} from "@/components/Operations";
import UserArea from "@/components/UserArea.vue";
import {cancelRedactionProcess, getTemporaryFileURL, signOnly, submitRedactionProcess} from "@/api";
import {RedactionProcess} from "@/dto/RedactionProcess";
// import { RefSymbol } from '@vue/reactivity';


const file = ref<File>();
const operation = ref(Operation.SIGN_ONLY)
const redactionProcess = ref<RedactionProcess>();
const redactedElemsTextBoxRef = ref<String>()

function setFile(newFile : File) {
  console.log("Added file: " + newFile.name)
  file.value = newFile;
  if(redactionProcess.value)
    cancelRedactionProcess(redactionProcess.value?.taskId)
        .finally(() => redactionProcess.value = undefined)

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

async function handleOperationButtonClick() {
  console.log(operation.value)
  if(!file.value)
    return

  switch (operation.value) {
    case Operation.SIGN_SELECT_REDACTABLE_ELEMS : {
      // await submitRedactionProcess(file.value!, Operation.SIGN_SELECT_REDACTABLE_ELEMS)
      break;
    }
    case Operation.SIGN_ONLY : {
      let blob = await signOnly(file.value!)
          .then(r => r.blob())
      downloadBlobRequest(blob)
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
a {
  color: #42b983;
}
</style>
