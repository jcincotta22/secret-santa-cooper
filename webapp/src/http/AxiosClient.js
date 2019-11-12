import axios from 'axios';
import getHost from './getHost';

const axiosClient = axios.create({
  baseURL: getHost()
});

export default axiosClient;