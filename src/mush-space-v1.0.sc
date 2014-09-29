//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// mush-space.sc
//
// Code for SuperCollider3
// mush space v1.0 is open source and is released under the MIT License.
//
// Copyright (c) 2013 Koichiro Mori
//                    http://moxus.org
//                    moxuse@gmail.com
//
// MIT License
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
// associated documentation files (the "Software"), to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify, merge, publish, distribute,
// sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or
// substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
// BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
// IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

s.boot;
Tempo.bpm = 123;
ProxySpace.push(s);

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// particle synth
//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

(
~x.play();
~x.source = {
	var mix;
	mix =(
		RHPF.ar(( FBSineC.ar(
			LFNoise2.kr(12200, 600, LFNoise2.kr(600, 200, LFNoise0.kr([10, 11], 12200, 1000))),
			LFNoise0.kr(8, 120, 20),
			LFNoise2.kr(0.2, 0.01, 1.01),
			LFNoise2.kr(0.3, 0.05, 0.8),
			LFNoise0.kr(0.4, 0.4, 0.3))
		*
		Gendy2.ar([1, 1.5], 2, 1, 1, minfreq: 1, maxfreq: 150, ampscale: 0.13, durscale: 0.128, initCPs: 8, mul: 0.8)
		),
		LFNoise0.kr(0.3, 30, 120).midicps, 0.4)
		* 24 ).softclip * 0.16;
	Out.ar(40, mix);
};
)

~x.end;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// synthDefs
//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////


(
SynthDef("hat1", {arg freq = 1000, amp = 1, gate = 1, rate = 68;
	var src;
	src = RHPF.ar(
		DynKlank.ar(`[[105, 128, 63, 83] * rate, 0.5, [1, 1, 1, 1]],
		ClipNoise.ar([0.015, 0.005]))
		* EnvGen.ar(Env.adsr(0.00, 0.3, 0.1, 0.1, 0.3, -6), gate, doneAction: 2),
	LFNoise2.ar(2, 23, 36).midicps,0.4);
	Out.ar(0, src * amp);
}).store;


SynthDef("hat2", {arg freq = 1000, amp = 1, gate = 1, rate = 38;
	var src;
	src = RLPF.ar(
		DynKlank.ar(`[[2105, 55, 2, 183]*rate, 0.5, [1, 1, 1, 1]],
		ClipNoise.ar([0.005, 0.015]))
		* EnvGen.ar(Env.adsr(0.00, 0.3, 0.1, 0.1, 0.3, -6), gate, doneAction: 2),
	LFNoise2.ar(2, 33, 120).midicps, 0.4);
	Out.ar(0, src * amp);
}).store;


SynthDef("fens", {arg amp = 1, gate = 1, fx = 23, rate = 1.0;
	var lo, sine, high, freq, mix, wet, mid, env;

	env = EnvGen.ar( Env.adsr(0.0, 1, 0.2, 3, 1, 12), gate, doneAction: 2);

  lo = DynKlang.ar(`[ [43, 51, 55].midicps, [0.5, 1, 0.6, 1.0], [1.0, 1.0, 1.0, 1.0] ], 0.5, 0 )!2 * Decay2.ar(Dust.ar(5) ,0.4, 1, 0.03);

	mid = Mix.fill( 15, {
    SinOsc.ar( ( [43, 51, 55].choose ).midicps * [0.3333, 1, 3, 6].choose + LFNoise2.kr(0.05, 3 ) * rate,
      0.3333,
      0.01
    )
  }).softclip;

	high =  (
		Mix.fill( 15, {
      SinOsc.ar( ( [43, 51, 55].choose ).midicps
        * [0.6666, 1, 3, 6, 9].choose + LFNoise2.kr(0.05, 3) * rate,
       0.0,
        * HenonC.ar({ SampleRate.ir / (2.rand + 1)},
          LFNoise2.kr(0.1, 0.4, 0.9),
          LFNoise2.kr(1, 1.15, 0.5)
        ) * 0.02
      )
    })
    * fx
	).softclip * 0.5;

	mix = CombC.ar(
    (high + lo + mid) * Decay2.ar(Dust.ar(8), 0.02, 0.5, amp) *env,
    0.01,
    {[0.02.rand, 0.03.rand] + 0.1},
    0.04
  );

	5.do( {mix = AllpassC.ar(mix, 0.04, { [0.03.rand, 0.03.rand] + 0.1 }, 0.08 ) } );

	Out.ar(40,mix);
}).store;


SynthDef("base",{arg freq = 440, gate = 1,amp = 1.0, knee = 4;
	var src, env, mod;

	mod = SinOsc.ar( freq,

		SinOsc.ar([freq, freq * 1.01],
			SinOsc.ar(freq * 3, 0, knee) * EnvGen.ar(Env.adsr(0.05, 0.15, 0.2, 0.1, 1, -2)),
			0.3
		) * EnvGen.ar(Env.adsr(0.0, 0.1, 0.3, 0.25, 1, -2)),

		SinOsc.ar(freq, 0, 0.3)
	);

	src = DynKlang.ar(`[ [freq, freq * 2, freq * 1.333333, freq * 0.5] + LFNoise2.kr(2, 6), [1, 0.5, 0.2, 0.5], [0.0, 0.5, 0.3, 0.5] ], 1, 0 )!2;

	env = EnvGen.ar(Env.adsr(0.3, 0.05, 0.2, 1, amp, 3), gate, doneAction: 2);

	Out.ar(40, HPF.ar((src - mod) * 0.3, 10) * env);

}).store();


SynthDef("base2", {arg freq = 440, gate = 1,amp = 1.0;
	var src, env;

	src = DynKlang.ar(`[ [freq, freq * 2, freq * 3, freq * 0.5] + LFNoise2.kr(2, 6), [1, 0.5, 0.2, 0.5], [0.0, 0.5, 0.3, 0.5] ], 1, 0 )!2;

	env = EnvGen.ar(Env.adsr(0.02, 0.1, 0.2, 0.6, amp, 12), gate, doneAction: 2);

	Out.ar(0, HPF.ar( (src) * 0.2, 10) * env);

}).store();


SynthDef("lead", {arg freq = 440, gate = 1, amp = 1;
	var osc, klank, high, mix;

	osc = Mix.fill(12, {
    var freq;
    freq = {[58, 77, 82].choose.midicps};
    SinOsc.ar(freq * [0.5, 1,2].choose + LFNoise2.kr(0.1, 5) ,
      SinOsc.ar( freq*0.5, SinOsc.ar(freq * 0.5, 0, 0.3), 0.5),
      LFNoise2.kr( 0.3, 0.1 )
    )
  }) * Decay2.ar(Impulse.ar(10), 0.01, 0.5);

	high =  RLPF.ar(
    Mix.fill( 21, {
      Pulse.ar(
        ((NamedControl.kr(\freqs, [43, 55], [0.2, 0.03])).choose).midicps * [1, 2, 3, 6, 1.3333].choose + LFNoise2.kr(0.05, 6),
        0.1,
        LFNoise2.kr(0.1, 0.1)
      )
      }
    ),
    LFNoise0.kr(0.4, 20, 120).midicps,
    0.3
  );

	klank = Klank.ar(`[[7500, 1278, 650, 93], nil, [1, 1, 0.8, 0.6]], Impulse.ar(920, 0.01, 0.1)!2) * LFNoise2.kr(0.3, 0.3);

	mix = (klank + osc + high) * Decay2.ar(Dust.ar(8 * 2.4, 1) * EnvGen.ar(Env.linen(0.0, 1, 0.3, 1, 2), gate, doneAction: 2), 0.0, 0.4, 1) ;

	Out.ar(50,mix);
}).store;

SynthDef("lead2", {arg freq = 440, gate = 1, amp = 1;
	var osc,osc2,mix;

	osc = Mix.fill(8, {
    SinOsc.ar(freq * [1, 0.5].choose + LFNoise2.kr(0.1, 0.5) ,
      SinOsc.ar( freq, SinOsc.ar(freq * 0.5, 0.25, EnvGen.ar(Env.perc(0.0, 0.08, 6, 2))), 2),
      LFNoise2.kr( 0.3, 0.1 )
    )
  });

	osc2 = Mix.fill(4, {
    SinOsc.ar(
      (freq * [0.25, 0.5].choose) +LFNoise2.kr(0.1, 0.5) ,
      SinOsc.ar(freq, SinOsc.ar(freq * 0.25, 0.25, EnvGen.ar(Env.perc(0.0, 0.08, 6, 2))), 2),
      LFNoise2.kr(0.3, 0.1)
    )
  });

	mix = (osc2 + osc) * EnvGen.ar(Env.linen(0.0, 0.125, 0.03, amp,6 ), gate, doneAction: 2) ;

	Out.ar(0, Pan2.ar(mix, 0.35));
}).store;


SynthDef("reverb_1", {
	var mix,in;
	in = In.ar([40, 41]);

	mix = in;

	5.do({ mix = AllpassC.ar(mix, 0.1, {0.03.rand + 0.1}, 0.3) });

	Out.ar(0, mix);
}).store();


SynthDef("reverb_2", {
	var mix, in;
	in = In.ar([50, 51]);

  mix = in;

	4.do({mix = AllpassC.ar(mix, 0.25, {[0.01.rand, 0.01.rand] + 0.01}, 0.2)});

	Out.ar(0, mix);
}).store();


SynthDef("click1", {arg out = 0, amp, rate;
	Out.ar(0,
		Pan2.ar(PlayBuf.ar(1, 1,rate * 0.75, 1.0, 0.0, 1),
      LFNoise2.ar(0.04)
    )
    * EnvGen.ar(Env.perc(0.0, 0.1), doneAction: 2, levelScale: amp)
	)
}).store;


SynthDef("click2", {arg out = 0, amp, rate;
	var src;
	src = PlayBuf.ar(1, 2,rate * 2.75, 1.0, 0.0, 1);
	Out.ar(40,
		Pan2.ar(src,
      LFNoise2.ar(0.04)
    )
    * EnvGen.ar(Env.perc(0.0, 0.2), doneAction: 2, levelScale: amp);
	)
}).store;


SynthDef("click3", {arg out=0,amp,rate;
	var src;
	src = (PlayBuf.ar(1, 4,rate*12, 1.0, 0.0, 1) * 120).softclip * 0.2;
	Out.ar(40,
		Pan2.ar(
      RLPF.ar(src,LFNoise2.ar(0.1,27,120).midicps,0.3),
      LFNoise2.ar(0.04)
    )
    * EnvGen.ar(Env.perc(0.0, 0.075, 1, 12), doneAction: 2, levelScale: amp);
	)
}).store;


SynthDef("kicks", {arg out = 0, amp, rate;
	Out.ar(0,
		Pan2.ar(
      PlayBuf.ar(1, 8,rate*0.98, 1.0,0.0,1),
      0
    )
    * EnvGen.ar(Env.perc(0.01,0.25), doneAction: 2, levelScale: amp);
	)
}).store;


SynthDef("click4", {arg out = 0, amp, rate;
	Out.ar(0,
		Pan2.ar(
      PlayBuf.ar(1, 3, rate, 1.0, 0.0 ,0),
      0
    )
    * EnvGen.ar(Env.perc(0.0, 0.3, 1.0, 12), doneAction: 2, levelScale: amp);
	)
}).store;

SynthDef("click5", {arg amp = 0, dur, rate;
	var out;
	out = Pan2.ar(Klank.ar(`[[50, 18492, 8354], nil, [0.6, 0.6, 1]], ClipNoise.ar(0.04))
		* amp
		* EnvGen.kr(Env.perc(0.01, 0.15 * dur, 0.77, -2), 1, doneAction: 2), 0.6);

	Out.ar(0,(out * 32).softclip * 0.12);
}).store;


SynthDef("click6", {arg rate = 1, amp = 0,trate = 860;
	var b = 3, dur, out;
	dur = SinOsc.ar(0.4, 0,43) / trate;
	out = TGrains.ar(2, Impulse.ar(XLine.ar(14, trate, 0.03)), b, rate, MouseX.kr(0, BufDur.kr(b)), dur, LFNoise2.kr(3), 4, 2)
  * amp
  * EnvGen.kr(Env.perc(0.01, 0.07, 0.05, 4), 1, doneAction: 2);
  Out.ar(0, out);
}).store;

SynthDef("snr0", {arg out = 0, amp, rate;
	var osc;
	osc = SinOsc.ar( Line.kr(6020, 40, 0.03), SinOsc.ar(420, 0, 1), 0.35).tanh;
	Out.ar(0,
		Pan2.ar(
      osc * EnvGen.ar(Env.perc(0.0, 0.1, 1, -3), doneAction: 2, levelScale: amp),
      -0.25
    )
	)
}).store;
)


(
SynthDef("lead", {arg freq = 440, gate = 1, amp = 1;
	var osc, klank, high, mix;

	osc = Mix.fill(15, {
    var freq;
    freq ={[58, 77].choose.midicps*0.6666};
    SinOsc.ar(freq * [0.5, 1,2].choose + LFNoise2.kr(0.1,5) ,
      SinOsc.ar( freq * 0.5, SinOsc.ar(freq * 0.5, 0, 0.3), 0.5),
      LFNoise2.kr( 0.3, 0.1 ))
    }
  )
  * Decay2.ar(Impulse.ar(10), 0.01, 0.5);

	high =  RLPF.ar(
    Mix.fill( 18, {
      Pulse.ar(
        ((NamedControl.kr(\freqs, [43, 55], [0.2, 0.03])).choose).midicps * [12, 3, 6, 1.3333].choose + LFNoise2.kr(0.05, 6),
        0.1,
        LFNoise2.kr(0.1, 0.1)
      )
  }),
    LFNoise0.kr(0.4, 20, 120).midicps,
    0.3
  );

	klank = Klank.ar( `[[7500, 1278, 650, 93], nil, [1, 1, 0.8, 0.6]], Impulse.ar(920, 0.01, 0.1)!2) * LFNoise2.kr(0.3, 0.3);

	mix = (klank + osc + high) * Decay2.ar(Dust.ar(12, 1) * EnvGen.ar(Env.linen(0.0, 1.3, 0.0, 1.2, -2), gate, doneAction:2), 0.2, 0.5, 1) ;

	Out.ar(50, (mix * 32).softclip * 0.15);
}).store;
)

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// load soundfiles for percussions
//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

(
s.sendMsg(\b_allocRead, 1, Platform.resourceDir +/+ "sounds/hh8.wav");
s.sendMsg(\b_allocRead, 2, Platform.resourceDir +/+ "sounds/pad4.wav");
s.sendMsg(\b_allocRead, 3, Platform.resourceDir +/+ "sounds/hh4.wav");
s.sendMsg(\b_allocRead, 4, Platform.resourceDir +/+ "sounds/bent7.wav");
s.sendMsg(\b_allocRead, 8, Platform.resourceDir +/+ "sounds/kick4.wav");
)


//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// main sequence patterns
//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

(
Pdef(\pattern,
	Ppar([

    ///////////// percussions

    Pbind(
      \instrument, Prand(["click2", "click5", "click1", "kicks"], inf),
      \dur, 0.5 * Pshuf([1, 0.5, 1 ,1 ,1 ,0.5], inf),
      \amp, Pseq([2, 1, 4, 1, \, 1, 4] / 4, inf),
      \rate, Pshuf([1, 1, -0.95, 0.8, -0.98, 1.02], inf)
    ),

		Pbind(
      \instrument, Prand(["kicks"],inf),
      \dur, 8,
      \amp, Pshuf([1], inf) * Pseq([1, 1,Pseq([0.85, 0.95, 0.75], 2), 1, 1, 1, 1], inf),
      \rate, Pshuf([1, 1, 0.975, 0.88, 0.98, 0.85], inf)
    ),

		Pbind(
      \instrument, Prand(["click5", "click5", "click1", "kicks"], inf),
      \dur, 0.5 * Pshuf([1, 0.5, 1, 1, 1, 0.5], inf),
      \amp, Pseq([2, 1, 4, 1, \, 1, 4] / 4, inf),
      \rate,Pshuf([1, 1, -0.95, 0.8, -0.98, 1.02], inf)
    ),

		Pbind(
      \instrument, Pshuf(["click4", "click2", "click6"], inf),
      \dur ,Pshuf([0.25, 0.75], inf),
      \amp, Pshuf([1, \, 1, 4] / 4, inf),
      \rate, Pshuf([1, 1, -0.95, 0.98, 1.05], inf)
    ),

		Pbind(
      \instrument, Pshuf(["click1", "click6"], inf),
      \dur, Pshuf([0.25, 0.5, 2, 4, 1, 1], inf) * Pshuf([1, 1, 1, 1, 0.75, Pseq([0.25, 0.5, 0.75], 9), 1, 0.25], inf),
      \amp, Pshuf([4, 3, 4, 1, \, 4] / 4, inf),
      \rate, Pshuf([1, 1, 0.75, 0.8, 0.5] * 8, inf)
    ),

		Pbind(
      \instrument, "click2",
      \dur, Prand([0.5, 1, 1, 0.5, 0.25, 4], inf) * Pshuf([1, 0.75, Pseq([0.25, 0.5, 0.75], 4), 1, 0.25], inf),
      \amp, Pshuf([4, 2, 4, \, 4] / 4, inf)
    ),

    ////////////// base

		Pbind(
      \instrument, \base,
      \dur, 1.0,
      \legato, Prand([0.03, 1, 0.23, 1.45, 2.0, 1.23], inf),
      \amp, Prand([0, 0.7, 0.8, 0,1, 0,0, 1,0], inf),
      \midinote, Pseq([41, 43, 46, 48, 55, 60, 46, 51, 53, 53, 43, 43, 51, 48, 51] - 12, inf),
      \knee, Prand([4, 10, 13], inf)
    ),

		Pbind(
      \instrument, \base,
      \dur, 0.75,
      \legato,Pseq([1.03, 0.1, 0.5, 4.45, 1.0, 0.3], inf),
      \amp, Prand([0, 0, 1.0, 0.8, 0], inf),
      \midinote,Prand([43, 43, 55] - 12, inf),
      \knee, Prand([4, 4, 4, 10, 13], inf)
    ),


		Pbind(
      \instrument, \fens,
      \dur, 8,
      \legato, Pseq([1.3, 2, 1.13, 0.5, 1.1, 1.23], inf),
      \rate, Pseq([0,5, -7].midiratio, inf),
      \amp, 1.2,
      \fx, Prand([3, 3, 3, 16, 13], inf)
    ),

		Pbind(
      \instrument, \lead,
      \dur, 8,
      \legato, 1,
      \amp, 1,
      \freqs, Pseq([[43, 55], [46, 51], [48, 53], [41, 51], [43, 46], [43, 48], [36, 51], [41, 51]], inf)
    ),

    Pbind(
    \instrument, \lead2,
    \dur, 0.25,
    \legato, 1,
    \amp, Pseq([Pseq([1], 11), Pseq([0], 21)], inf),
    \midinote, Prand([0, 3, 5, 7, 12, 15, 17] + 67, inf),
    \lag, 2.0
    ),

		Pbind(
      \instrument, \hat1,
      \dur, Pseq([0.25, 0.25, 0.25, 0.75], inf),
      \legato, Pseq([0.03, 1, 0.23, 1.45, 0.1, 0.23], inf),
      \amp, Prand([0, 1, 1, 1, 0], inf)
    ),

		Pbind(
      \instrument, \hat2,
      \dur, Pseq([0.25, 0.25, 0.25, 0.75],  inf),
      \legato, Pshuf([0.03, 1, 0.23, 1.45, 0.1, 0.23], inf),
      \amp, Prand([0, 0, 1, 1, 0], inf)
    )

	])
).play();
)

Pdef(\pattern).stop;


//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// reverb synthes
//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

(
s.sendMsg(9, \reverb_1, 2001, 1, 0, 1);
s.sendMsg(9, \reverb_2, 2002, 1, 0, 1);
)

// remove \reverbs
(
s.sendMsg(11, 2001);
s.sendMsg(11, 2002);
)

