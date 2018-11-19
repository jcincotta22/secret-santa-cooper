import React from "react";
import './styles.css';

const User = ({ openModal, userId, name }) => {
  return (
    <div className='user' onClick={() => openModal(userId, name)}>Click For {name}</div>
  );
};

export default User;