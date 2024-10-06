<template>
  <body>
    <div class="container">
      <h1>404</h1>
      <p>Page Not Found</p>
      <p>The page you are looking for does not exist on Project Pulse.</p>
      <router-link to="/">Go to Homepage</router-link>
    </div>
  </body>
</template>

<script setup lang="ts"></script>

<style lang="scss" scoped>
body {
  margin: 0;
  padding: 0;
  font-family: 'Arial', sans-serif;
  background-color: #f0f0f0;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}

.container {
  text-align: center;
  padding: 20px;
  background: white;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

h1 {
  font-size: 100px;
  margin: 0;
  color: #ff6347;
}

p {
  font-size: 20px;
  margin: 10px 0;
}

a {
  display: inline-block;
  margin-top: 20px;
  padding: 10px 20px;
  color: white;
  background-color: #ff6347;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.3s ease;

  &:hover {
    background-color: #ff4500;
  }
}
</style>
