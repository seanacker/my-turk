<template>
  <div class="App">
    <transition name="fade" mode="out-in">
      <Nuxt />
    </transition>
  </div>
</template>

<style lang="scss">
.App {
  color: #2c3e50;
  padding: 20px;
  height: 100%;

  > * {
    max-width: 1400px;
    margin: 0 auto;
  }
  @media (min-width: breakpoint(tablet-portrait)) {
    padding: 40px;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition-duration: 0.4s;
  transition-property: opacity;
  transition-timing-function: $ease;
}

.fade-enter,
.fade-leave-active {
  opacity: 0;
}
</style>
