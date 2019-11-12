import AxiosClient from './AxiosClient';

export const login = ({ username, password, userId }) => {
  const basicAuth = 'Basic ' + btoa(username + ':' + password);
  return AxiosClient.get(`/api/users/${userId}`,
    { headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      'Authorization': basicAuth,
      }
    })
};

export const getAllUsers = () => {
  return AxiosClient.get(`/api/users`,
    { headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      }
    })
};