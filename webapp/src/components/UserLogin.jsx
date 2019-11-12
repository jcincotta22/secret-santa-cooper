import React from 'react';
import ReactModal from 'react-modal';
import Button from './Button';
import Input from './Input';
import { login, getAllUsers } from '../http/http';
import User from './User';
import './styles.css';
import SecretSanta from './SecretSanta';

export default class UserLogin extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      username: '',
      password: '', 
      isOpen: false,
      secretSanta: '',
      invalidLogin: '',
      currentUserId: null,
      currentUser: null,
      allUsers: [],
    }
  }

  componentDidMount() {
    getAllUsers().then(({ data }) => {
      this.setState({allUsers: data})
    })
  }

  closeModal = () => {
    this.setState({ isOpen: false, invalidLogin: '', currentUser: null, currentUserId: null })
  }

  openModal = (id, name) => {
    this.setState({ 
      isOpen: true,
      currentUserId: id, 
      currentUser: name,
    })
  }

  handleFormSubmit = (e) => {
    e.preventDefault();
    const userId = this.state.currentUserId;
    login({ username: this.state.username.toLowerCase(), password: this.state.password, userId }).then(({ data }) => {
      this.setState({ secretSanta: data })
    })
    .catch(e => this.setState({ invalidLogin: 'Invalid login please try again' }))
  }

  handleUsername = (e) => {
    this.setState({ username: e.target.value })
  }

  handlePassword = (e) => {
    this.setState({ password: e.target.value })
  }

  onKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      e.stopPropagation();
      this.handleFormSubmit(e);
    }
  }


  render() {
    const { isOpen, username, password, secretSanta, invalidLogin, allUsers } = this.state;
    const modalStyle = {
      content: {
        width: window.innerWidth > 550 ? '35%' : '60%',
        margin: 'auto',
        height: '20%',
        display: 'flex',
        justifyContent: 'center'
      }
    }
    return(
      <div className='user-container'>
        {!secretSanta && allUsers ? allUsers.map(user => <User key={user._idx} openModal={this.openModal} userId={user._id} name={user.name} />) : null}
        {secretSanta ? <SecretSanta text={secretSanta} userName={this.state.currentUser} />
        : 
        <ReactModal
          isOpen={isOpen}
          shouldCloseOnEsc={true}
          shouldCloseOnOverlayClick={true}
          onRequestClose={this.closeModal}
          ariaHideApp={false}
          style={modalStyle}
          overlayClassName={'overlay'}
        >
          <form className="form-container" onSubmit={this.handleFormSubmit} onKeyDown={this.onKeyDown}>
            {invalidLogin ? <div className='error'>{invalidLogin}</div> : null }
            <Input
              inputType={"text"}
              title={"Username"}
              name={"username"}
              value={username}
              placeholder={"Enter your name"}
              handleChange={this.handleUsername}
            />
            <Input
              inputType={"password"}
              name={"password"}
              title={"Password"}
              value={password}
              placeholder={"Enter password"}
              handleChange={this.handlePassword}
            />
            <div className='button-container'>
              <Button
                action={this.closeModal}
                type={"primary"}
                title={"Close"}
              />
              <Button
                action={this.handleFormSubmit}
                type={"primary"}
                title={"Submit"}
              />
            </div>
          </form>
        </ReactModal>}
      </div>
    )
  }
}