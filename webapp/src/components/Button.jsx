import React from "react";

const Button = props => {
  return (
    <div className='form-group-button'>
      <button
        className='button'
        onClick={props.action}
      >
        {props.title}
      </button>
    </div>
  );
};

export default Button;