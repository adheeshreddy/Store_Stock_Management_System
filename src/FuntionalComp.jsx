import React, { useState } from 'react';

function FunctionalComp() {
  const [count, setCount] = useState(0); // Using the useState hook for state management

  const increment = () => {
    setCount(count + 1);
  };

  return (
    <div>
      <h1>Functional Counter</h1>
      <p>Count: {count}</p>
      <button onClick={increment}>Increment</button>
    </div>
  );
}

export default FunctionalComp;