import React from 'react';
import ReactModal from 'react-modal';
import Button from './Button';
import Input from './Input';
import { login, getAllUsers } from '../http/http';
import './styles.css';
import SecretSanta from './SecretSanta';
import Select from 'react-select';

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
      selectedOption: null,
    }
  }

  componentDidMount() {
    getAllUsers().then(({ data }) => {
      this.setState({ allUsers: data })
    })
  }

  closeModal = () => {
    this.setState({ isOpen: false, invalidLogin: '', currentUser: null, currentUserId: null, selectedOption: null })
  }

  openModal = () => {
    this.setState({ 
      isOpen: true,
    });
  }

  setUser = (user) => {
    this.setState({ 
      currentUserId: user.value._id, 
      currentUser: user.value.username,
      selectedOption: user,
    });
  }

  handleFormSubmit = (e) => {
    e.preventDefault();
    const userId = this.state.currentUserId;
    login({ username: this.state.currentUser, password: this.state.password, userId }).then(({ data }) => {
      this.setState({ secretSanta: data })
    })
    .catch(e => this.setState({ invalidLogin: 'Invalid login please try again' }))
  }

  handleUsername = (e) => {
    this.setState({ username: e.target.value })
  }

  handlePassword = (e) => {
    this.setState({ password: e.target.value, invalidLogin: '' })
  }

  onKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      e.stopPropagation();
      this.handleFormSubmit(e);
    }
  }

  render() {
    const { isOpen, password, secretSanta, invalidLogin, allUsers, currentUserId, selectedOption } = this.state;
    const modalStyle = {
      content: {
        width: '20em',
        margin: 'auto',
        height: '20%',
        display: 'flex',
        justifyContent: 'center',
        minHeight: '125px',
      }
    }
    const userOptions = allUsers.map(u => ({ value: u, label: u.name }));

    return(
      <div className='user-container'>
        <div className={'select-user-form-container'}>
          {!secretSanta ? <div className='select-container'>
            <Select 
              value={selectedOption}
              placeholder={'Select your name...'}
              options={userOptions} 
              onChange={(u) => this.setUser(u)} 
            />
          </div> : null}

          {!secretSanta && currentUserId ? <div className='button-container'>
            <Button
              action={() => this.openModal()}
              type={"primary"}
              title={"REVEAL"}
            />
          </div> : null}
        </div>
        
        
        {secretSanta ? <SecretSanta text={secretSanta} userName={this.state.selectedOption.value.name} />
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
            <div className='input-button-container'>
              <div className='password-label'>Enter your password</div> 
              <div className='input-container'>
                <Input
                  className='password'
                  inputType={"password"}
                  name={"password"}
                  value={password}
                  placeholder={"Enter your password"}
                  handleChange={this.handlePassword}
                />
              </div>
              <div className='button-container'>
                <Button
                  action={this.closeModal}
                  type={"primary"}
                  title={"CLOSE"}
                />
                <Button
                  action={this.handleFormSubmit}
                  type={"primary"}
                  title={"REVEAL"}
                />
              </div>
              {invalidLogin ? <div className='error'>{invalidLogin}</div> : null }
            </div>
            
          </form>
        </ReactModal>}
      </div>
    )
  }
}