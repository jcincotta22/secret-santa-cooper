import React from "react";

const SecretSanta = ({ text, userName }) => {
  return (
    <div className="secret-santa-container">
      {userName}, you got... <span className='secret-santa'>{text}!</span>
    </div>
  );
};

export default SecretSanta;