<template>
  <div id="warning">
    Attention: This is a proof of concept application intended solely for demonstration and research purposes.
    Never use this in production!
  </div>
  <img alt="Vue logo" src="./assets/logo.png" style="margin-top: 20px">
  <h1>PDF HTML Redactable Signatures Platform</h1>
  <RouterView/>
</template>

<script setup lang="ts">
// import { Options, Vue } from 'vue-class-component';
// import HelloWorld from './components/MainView.vue';
// import Login from "@/components/Login.vue";
//
// @Options({
//   components: {
//     Login,
//     HelloWorld,
//   },
// })
// export default class App extends Vue {
//
// }

</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

#warning {
  width: 100%;
  background-color: yellow;
  top: 0;
  left: 0;
  position: absolute;
  text-align: center;
}
</style>
