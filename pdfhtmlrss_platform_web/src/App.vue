<template>
  <div id="warning" style="z-index: 1">
    Attention: This is a proof of concept application intended solely for demonstration and research purposes.
    Never use this in real world applications!
  </div>
  <div class="app-view">
    <div class="control-view">
      <img alt="Vue logo" src="./assets/logo.png" style="margin-top: 20px">
      <h1>PDF HTML Redactable Signatures Platform</h1>
      <RouterView @open-document-view="handleOpenDocumentView" @close-document-view="handleCloseDocumentView"/>
    </div>
<!--    <div class="separator" v-if="documentViewToggle" @mousedown="handleSeparatorResize" @touchdown="handleSeparatorResize"/>-->
    <div class="separator" v-if="documentViewToggle"/>
    <div class="document-view" v-if="documentViewToggle" ref="documentView">
<!--      <iframe src="http://192.168.56.3:8081/lorem_ipsum.html" ></iframe>-->
<!--      <iframe src="http://192.168.56.3:8081/invoice_example.pdf" ></iframe>-->
      <iframe :src="documentViewUrl" :width="documentViewWidth ?? 'auto'"></iframe>
    </div>
<!--    <div :style="{height : '100%'}" v-if="viewToggle">-->
<!--    </div>-->
  </div>
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

import { ref } from 'vue';

const documentViewToggle = ref<boolean>(false);
const documentViewUrl = ref<string>("");
const documentViewWidth = ref<number | undefined>(undefined);
const documentView = ref(null)

function handleOpenDocumentView(url : string) {
  documentViewUrl.value = url;
  documentViewToggle.value = true;
}

function handleCloseDocumentView() {
  documentViewToggle.value = false;
  documentViewUrl.value = "";
}

// function handleSeparatorResize(e : MouseEvent) {
//   console.log(documentView.value?.$el?.clientWidth);
//   documentView.value.width += e.offsetX;
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

.app-view {
  position: absolute;
  display: flex;
  justify-content: space-around;
  height: 100%;
  width: 100%;
  flex-direction: row;
}

.control-view {
  flex-grow: 1;
}

.separator {
  width: 20px;
  background-color: black;
  cursor: col-resize;
}

.document-view {
  height: 100%;
  flex-grow: 1;
}

.document-view iframe {
  position: relative;
  height: 100%;
  width: 100%;
}
</style>
