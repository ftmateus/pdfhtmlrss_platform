<template>
  <div class="hello">
    <h1>{{ msg }}</h1>
    <div style="
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content : space-between;
      height: 225px;
    ">
      <ActionsSelector/>
      <FileSelector/>
      <OperationButton/>
    </div>
  </div>
</template>

<script lang="ts">
import { Options, Vue } from 'vue-class-component';
import FileSelector from "@/components/FileSelector.vue";
import ActionsSelector from "@/components/OperationsSelector.vue";
import OperationButton from "@/components/OperationButton.vue";

@Options({
  components: {OperationButton, ActionsSelector, FileSelector},
  props: {
    msg: String
  }
})
export default class HelloWorld extends Vue {
  msg!: string
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
