<template>
  <img alt="Vue logo" src="./assets/logo.png">
  <HelloWorld msg="PDF HTML Redactable Signatures Platform"/>
</template>

<script lang="ts">
import { Options, Vue } from 'vue-class-component';
import HelloWorld from './components/HelloWorld.vue';
import * as api from "@/api";

@Options({
  components: {
    HelloWorld,
  },
})
export default class App extends Vue {

}

api.testApi()
    .then(r => r.text())
    .then(t => console.log(t))

</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
