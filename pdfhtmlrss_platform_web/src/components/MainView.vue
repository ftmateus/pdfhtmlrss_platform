<template>
  <div class="hello">
    <div style="
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content : space-between;
      height: 225px;
    ">
      <OperationsSelector v-model="operation"/>
      <FileSelector :set-file="setFile"/>
      <OperationButton :operation="operation"/>
    </div>
  </div>
</template>

<script lang="ts">
import {Options, Vue} from 'vue-class-component';
import FileSelector from "@/components/FileSelector.vue";
import OperationsSelector from "@/components/OperationsSelector.vue";
import OperationButton from "@/components/OperationButton.vue";
import {Operation} from "@/components/Operations";

@Options({
  components: {OperationButton, OperationsSelector, FileSelector},
})
export default class HelloWorld extends Vue {

  file : File | null = null;
  operation : Operation = Operation.SIGN_ONLY

  setFile : Function = (newFile : File) => {
    console.log("Hello World")
    this.file = newFile;
  }
}

</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
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
