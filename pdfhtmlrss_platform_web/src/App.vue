<template>
  <img alt="Vue logo" src="./assets/logo.png">
  <h1>PDF HTML Redactable Signatures Platform</h1>
  <HelloWorld msg="PDF HTML Redactable Signatures Platform"/>
<!--    <Login></Login>-->
</template>

<script lang="ts">
import { Options, Vue } from 'vue-class-component';
import HelloWorld from './components/MainView.vue';
import * as api from "@/api";
import Login from "@/components/Login.vue";

@Options({
  components: {
    Login,
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
