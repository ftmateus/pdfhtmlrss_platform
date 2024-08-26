<script setup lang="ts">
import {Ref, ref, defineProps} from "vue";
import {signOnly} from "@/api";

const props = defineProps({
  setFile : {
    type : Function,
    required : true
  }
})

const fileInputRef: Ref<any> = ref(null)

async function handleFileUpload() {

  const file = fileInputRef.value.files[0]

  props.setFile(file)

  // const response = await signOnly(file)
  // const data = await response.blob()
  //
  // const link = document.createElement('a')
  // link.href = URL.createObjectURL(data)
  // link.download = file.name
  // link.click()
  // URL.revokeObjectURL(link.href)
}
</script>

<template>
  <div
      id="drop_zone"
      ondrop="handleFileUpload"
      ondragover="dragOverHandler(event);"
  >
    <p>Drag file to this <i>drop zone</i>.</p>
    <input
        type="file"
        id="fileElem"
        accept="application/pdf,text/xml,text/html"
        class="visually-hidden"
        ref="fileInputRef"
        @input="handleFileUpload"
    />
    <!--    <label for="fileElem">Or select file</label>-->
  </div>
</template>

<style scoped>
#drop_zone {
  border: 1px solid black;
  width: 500px;
  height: 100px;
  border-radius: 20px;
}

</style>