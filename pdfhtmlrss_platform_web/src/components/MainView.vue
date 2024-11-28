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
      <form v-if="isSelectRedactableElems()">
        <input type="radio" name="sign_option" id="compatibility" checked>
        <label for="compatibility">Improved compatibility</label>
        <input type="radio" name="sign_option" id="size">
        <label for="size">Smaller file</label>
      </form>
      <OperationButton
          :operation="operation"
          @click="handleOperationButtonClick"
          :no-file-selected="file == null"
      />
    </div>
  </div>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-class-component';
import FileSelector from "@/components/FileSelector.vue";
import OperationsSelector from "@/components/OperationsSelector.vue";
import OperationButton from "@/components/OperationButton.vue";
import { Operation } from "@/components/Operations";
import UserArea from "@/components/UserArea.vue";
import {signOnly, submitRedactionProcess} from "@/api";

@Options({
  components: {UserArea, OperationButton, OperationsSelector, FileSelector},
})
export default class MainView extends Vue {

  file : File | null = null;
  operation : Operation = Operation.SIGN_ONLY

  setFile(newFile : File) {
    console.log("Added file: " + newFile.name)
    this.file = newFile;
  }

  isSelectRedactableElems() {
    return this.operation == Operation.SIGN_SELECT_REDACTABLE_ELEMS
  }

  async handleOperationButtonClick() {
    console.log(this.operation)
    if(this.file == null)
      return

    switch (this.operation) {
      case Operation.SIGN_SELECT_REDACTABLE_ELEMS : {
        await submitRedactionProcess(this.file!, Operation.SIGN_SELECT_REDACTABLE_ELEMS)
        break;
      }
      case Operation.SIGN_ONLY : {
        let blob = await signOnly(this.file!)
            .then(r => r.blob())
        let fileUrl = window.URL.createObjectURL(blob);
        let a = document.createElement('a');
        a.href = fileUrl;
        a.download = this.file?.name ?? "";
        document.body.appendChild(a); // we need to append the element to the dom -> otherwise it will not work in firefox
        a.click();
        a.remove();
        break;
      }
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
