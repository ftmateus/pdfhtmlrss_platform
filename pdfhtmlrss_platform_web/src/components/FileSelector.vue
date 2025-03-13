<script setup lang="ts">
import {Ref, ref, defineProps} from "vue";

const props = defineProps({
  setFile : {
    type : Function,
    required : true
  }
})

const fileInputRef: Ref<any> = ref("fileInputRef")

async function handleFileUpload() {
  const file = fileInputRef.value.files[0]
  props.setFile(file)
}
function handleFileDrop(e : DragEvent) {
  if(!e.dataTransfer?.files?.length)
    return;

  const file : File = e.dataTransfer.files[0]
  fileInputRef.value.files = e.dataTransfer.files
  props.setFile(file)
}

</script>

<template>
  <div
      id="drop_zone"
      @drop.prevent="handleFileDrop"
      @dragenter.prevent
      @dragleave.prevent
      @dragover.prevent
  >
    <slot></slot>
    <input
        type="file"
        id="fileElem"
        accept="application/pdf,text/xml,text/html"
        style="background-color: transparent; border-radius: 0px"
        ref="fileInputRef"
        @input="handleFileUpload"
    />
    <p style="font-size: 10px">Maximum size: 5 MB</p>
    <!--    <label for="fileElem">Or select file</label>-->
  </div>
</template>

<style scoped>
#drop_zone {
  border: 1px solid black;
  width: 480px;
  border-radius: 20px;
  background-color: lightgray;
  box-sizing: border-box;
  padding-right: 20px;
  padding-left: 20px;
}


</style>