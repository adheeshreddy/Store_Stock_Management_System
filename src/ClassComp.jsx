import React, { Component } from 'react';

class ClassComp extends Component {
  constructor(props) {
    super(props);
    this.state = {
      count: 0, // Initializing state in the constructor
    };
  }

  increment = () => {
    this.setState({ count: this.state.count + 1 }); // Updating state with setState
  };

  render() {
    return (
      <div>
        <h1>Class Counter</h1>
        <p>Count: {this.state.count}</p>
        <button onClick={this.increment}>Increment</button>
      </div>
    );
  }
}

export default ClassComp;