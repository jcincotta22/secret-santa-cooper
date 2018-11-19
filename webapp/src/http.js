import axios from 'axios';

export const login = ({ username, password, userId }) => {
  const basicAuth = 'Basic ' + btoa(username + ':' + password);
  return axios.get(`/api/users/${userId}`,
    { headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      'Authorization': basicAuth,
      }
    })
};

export const getAllUsers = () => {
  return axios.get(`/api/users`,
    { headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      }
    })
};