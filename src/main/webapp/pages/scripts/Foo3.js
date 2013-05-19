/**
 * @param string /
 *            object / array
 * @returns Foo object
 */
var debug_foo = false;

function Foo() {
	// TODO remove debug
	if (debug_foo) {
		if (++this.constructors.constructorRecusiveDepth > 100) {
			console.error('too much recusive...');
			return;
		}
	}

	if (arguments.length > 2)
		console.warn('too many arguments: ' + arguments.length);

	if (arguments.length >= 1) {
		var o = arguments[0];
		var t;
		if (arguments.length >= 2)
			t = arguments[1];
		else
			t = this.getConstructorType(o);

		if (this.constructors.hasOwnProperty(t)) {
			this.constructors[t](this, o);
		} else {
			console.warn('unsupported object type: ' + t);
		}
	}

	// TODO remove debug
	if (debug_foo) {
		--this.constructors.constructorRecusiveDepth;
	}
}

Foo.prototype.__class__ = 'tk.blizz.Foo';

// get parameter type
Foo.prototype.getConstructorType = function(o) {
	var t = typeof o;
	if (t === 'object') {
		if (o instanceof Foo)
			t = 'foo';
		else if (o.hasOwnProperty('__class__')
				&& o.__class__ === Foo.prototype__class__)
			t = 'json';
		else if (o instanceof String)
			t = 'string';
		else if (o instanceof Array) // FIXME: construct from array ???????
			t = 'array';
		else if (o === null)
			t = 'nil';
		// else t is 'object'
	}

	return t;
};

// constructors
Foo.prototype.constructors = {

	// for recusive construct
	cache : function() {
		var c = [];
		return {
			add : function(k, v) {
				c[k] = v;
			},
			get : function(k) {
				return c[k];
			},
			remove : function(k) {
				delete c[k];
			},
			removeByVal : function(v) {
				for ( var k in c) {
					if (c[k] === v)
						delete c[k];
				}
			},
			find : function(v) {
				for ( var k in c) {
					if (c[k] === v)
						return k;
				}
				return;
			},
			clear : function() {
				for ( var k in c) {
					delete c[k];
				}
			}
		};
	}(),
	foo : function fromFoo(self, o) {
		// copy self vars, functions, do NOT copy prototypes
		// note: process recusive construct
		this.cache.add(o.getId(), self);

		for ( var k in o) {
			if (!o.hasOwnProperty(k))
				continue;

			var v = o[k];

			// do NOT copy class type
			if (k === '__class__' && v === Foo.prototype.__class__
					|| k === '__id__')
				continue;

			var rval = function process(c, v) {
				if (v instanceof Array) {
					var arr = new Array();
					for ( var i in v) {
						var j = v[i];
						if (typeof j !== 'function') {
							var r = process(c, j);
							if (typeof r !== 'undefined')
								arr[i] = r;
						}
					}
					return arr;
				} else if (v instanceof Foo) {
					// find in cache
					var m = c.get(v.getId());
					if (m)
						return m;
					else
						return new Foo(v, 'foo');
				} else
					return v;

			}(this.cache, v);

			if (typeof rval !== 'undefined')
				self[k] = rval;

		}

		this.cache.remove(o.getId());
		o.deserialize();
	},
	json : function fromJSON(self, o) {
		// note: process recusive construct
		if (o.__id__)
			this.cache.add(o.__id__, self);
		
		for ( var k in o) {
			if (!o.hasOwnProperty(k))
				continue;

			var v = o[k];
			if (k === '__class__' && v === Foo.prototype.__class__
					|| k === '__id__')
				continue;

			var rval = function process(c, v) {
				if (v instanceof Array) {
					var arr = new Array();
					for ( var i in v) {
						var j = v[i];
						if (typeof j !== 'function') {
							var r = process(c, j);
							if (typeof r !== 'undefined'
									&& typeof r !== 'function')
								arr[i] = r;
						}
					}
					return arr;
				} else if (typeof v === 'object'
						&& v.hasOwnProperty('__class__')
						&& v.__class__ === Foo.prototype.__class__) {

					if (v.__id__) {
						// find in cache
						var m = c.get(v.__id__);
						if (m)
							return m;
					}
					// json type, recusive construct
					return new Foo(v, 'json');
				} else
					return v;

			}(this.cache, v);

			if (typeof rval !== 'undefined' && typeof rval !== 'function')
				self[k] = rval;
		}

		// process all ref
		var visited = [];
		(function traverse(o, c) {
			if (visited.indexOf(o) >= 0)
				return;

			if (o instanceof Foo)
				visited.push(o);

			for ( var k in o) {
				if (!o.hasOwnProperty(k))
					continue;
				var v = o[k];
				if (typeof v === 'string') {
					var m = v.match(/^__id__@(\d+)$/);
					if (m) {
						var f = c.get(parseInt(m[1]));
						if (f)
							o[k] = f;// json type, recusive construct
					}
				} else if (v instanceof Foo || v instanceof Array)
					traverse(v, c);
			}
		})(self, this.cache);

		if (o.__id__)
			this.cache.remove(o.__id__);
	},
	string : function fromString(self, s) {
		var o = JSON.parse(s);
		this.json(self, o);
	},
	array : function fromArray(self, arr) {
		// console.debug(' ------- construct from array is not supported yet
		// ------');
	},
	object : function fromObject(self, o) {
		// console.debug('construct from object: ' + o);
		for ( var k in o) {
			if (!o.hasOwnProperty(k))
				continue;

			var v = o[k];
			var rval = function process(c, v) {
				if (v instanceof Array) {
					var arr = new Array();
					for ( var i in v) {
						var j = v[i];
						if (typeof j !== 'function') {
							var r = process(c, j);
							if (typeof r !== 'undefined')
								arr[i] = r;
						}
					}
					return arr;
				} else if (v instanceof Foo) {
					// find in cache
					var m = c.get(v.getId());
					if (m)
						return m;
					else
						return new Foo(v, 'foo');
				} else if (v.hasOwnProperty('__class__')
						&& v.__class__ === Foo.prototype__class__) {
					if (v.__id__) {
						// find in cache
						var m = c.get(v.__id__);
						if (m)
							return m;
					}
					// json type, recusive construct
					return new Foo(v, 'json');
				}

				else
					return v;

			}(this.cache, v);

			if (typeof rval !== 'undefined')
				self[k] = rval;
		}
	},
	nil : function fromNull(self, o) {
	}// do nothing...
};

// TODO remove debug
if (debug_foo) {
	Foo.prototype.constructors.constructorRecusiveDepth = 0;
}

// used before to json string
Foo.prototype.serialize = function() {
	if (!this.hasOwnProperty('__class__')) { // copy from proto __class__ to
		// instance class
		var cls = this.__class__;
		this.__class__ = cls;
	}
	this.getId();
	return this;
};

Foo.prototype.deserialize = function() {
	if (this.hasOwnProperty('__class__')) { // copy from proto __class__ to
		// instance class
		if (this.__class__ === Foo.prototype.__class__) {
			delete this.__class__;
		}
	}
	if (this.hasOwnProperty('__id__')) { // copy from proto __class__ to
		// instance class
		delete this.__id__;
	}
	return this;
};

Foo.prototype.getId = function() {
	if (!this.hasOwnProperty('__id__'))
		this.__id__ = this.nextId();
	return this.__id__;
};

// to generate uniq id
Foo.prototype.nextId = function() {
	var id = 0;
	if (arguments.length !== 0 && typeof arguments[0] === 'number')
		id = Math.floor(arguments[0]);

	return function() {
		return id++;
	};
}(0);

// convert object to json string
Foo.prototype.toJSONString = function() {
	var seen = [];

	var str = JSON.stringify(this, function(k, v) {
		if (typeof v === 'object') {

			if (seen.indexOf(v) >= 0) {
				if (v.hasOwnProperty('__id__'))
					return '__id__@' + v.__id__;
				else
					return;
			}
			if (v instanceof Foo) {
				v.serialize();
			} else if (!v.hasOwnProperty('__id__')) // plain object
				v.__id__ = Foo.prototype.nextId();
			seen.push(v);
		}
		return v;
	});

	// clean up
	for ( var k in seen) {
		var v = seen[k];
		if (v instanceof Foo) {
			v.deserialize();
		} else if (typeof v === 'object' && v.hasOwnProperty('__id__'))
			delete v[__id__];
	}

	return str;
};

// tests

console.log('---------------  test Foo tree ---------------------');
var t = new Foo();
var p = new Foo();
var l = new Foo();
var r = new Foo();

t.name = 'top_most';
p.name = 'ppp';
l.name = 'lll';
r.name = 'rrr';

t.next = p;
p.self = p;
p.left = l;
p.right = r;

l.parent = p;
r.parent = p;

l.next = r;
r.next = l;

console.log(t);
console.log(t.toJSONString());

console.log('-----------------test construct from Foo -----------------');
var n = new Foo(t);
n.name = 'nnnn from top_most';
console.log(n);

console
		.log('-----------------test construct from JSON string -----------------');
var m = new Foo(new String(n.toJSONString()));
m.name = 'mmmmmmmmmmm from n json';
console.log(m);

console
		.log('-----------------test construct from generic object  -----------------');
m = new Foo({
	name : "Hello",
	age : 19
});
console.log(m);

console.log('-----------------test construct from array  -----------------');
m = new Foo([ 'hello', 'world' ]);
console.log(m);
