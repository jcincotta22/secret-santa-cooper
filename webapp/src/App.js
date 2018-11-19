import React, { Component } from 'react';
import './App.css';
import UserLogin from './components/UserLogin';

class App extends Component {
  render() {
    return (
      <div className="App">
        <h2>Secret Santa</h2>
        <a href='/'>Refresh</a>
        <UserLogin />
      </div>
    );
  }
}

export default App;
