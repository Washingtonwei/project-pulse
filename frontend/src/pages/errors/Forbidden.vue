<template>
  <body>
    <div class="container">
      <h1>403</h1>
      <p>Forbidden</p>
      <p>You don't have permission to access this resource on Project Pulse.</p>
      <RouterLink to="/">Go to Homepage</RouterLink>
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
