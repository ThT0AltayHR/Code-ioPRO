import React, { useEffect, useRef } from 'react';
import { Animated, Dimensions, StyleSheet, View } from 'react-native';

const { width } = Dimensions.get('window');
const SIZE = Math.min(width - 32, 320);

interface Node {
  x: number;
  y: number;
  anim: Animated.Value;
  pulseAnim: Animated.Value;
  size: number;
  delay: number;
}

const RAW_NODES = [
  { x: 0.5,  y: 0.12, size: 4 },
  { x: 0.35, y: 0.22, size: 3 },
  { x: 0.65, y: 0.22, size: 3 },
  { x: 0.22, y: 0.35, size: 5 },
  { x: 0.50, y: 0.30, size: 6 },
  { x: 0.78, y: 0.35, size: 5 },
  { x: 0.12, y: 0.50, size: 3 },
  { x: 0.38, y: 0.48, size: 4 },
  { x: 0.62, y: 0.48, size: 4 },
  { x: 0.88, y: 0.50, size: 3 },
  { x: 0.25, y: 0.62, size: 5 },
  { x: 0.50, y: 0.60, size: 7 },
  { x: 0.75, y: 0.62, size: 5 },
  { x: 0.38, y: 0.75, size: 4 },
  { x: 0.62, y: 0.75, size: 4 },
  { x: 0.50, y: 0.85, size: 3 },
  { x: 0.15, y: 0.42, size: 3 },
  { x: 0.85, y: 0.42, size: 3 },
  { x: 0.30, y: 0.55, size: 3 },
  { x: 0.70, y: 0.55, size: 3 },
];

const CONNECTIONS = [
  [0,1],[0,2],[1,3],[1,4],[2,4],[2,5],[3,6],[3,7],[4,7],[4,8],[5,8],[5,9],
  [6,10],[7,10],[7,11],[8,11],[8,12],[9,12],[10,13],[11,13],[11,14],[12,14],[13,15],[14,15],
  [3,16],[5,17],[6,16],[9,17],[7,18],[8,19],[10,18],[12,19],
];

function distance(a: typeof RAW_NODES[0], b: typeof RAW_NODES[0]) {
  return Math.sqrt(Math.pow((a.x - b.x) * SIZE, 2) + Math.pow((a.y - b.y) * SIZE, 2));
}

function angle(a: typeof RAW_NODES[0], b: typeof RAW_NODES[0]) {
  return Math.atan2((b.y - a.y) * SIZE, (b.x - a.x) * SIZE) * (180 / Math.PI);
}

export default function BrainAnimation() {
  const nodes = useRef<Node[]>(
    RAW_NODES.map((n, i) => ({
      ...n,
      anim: new Animated.Value(0),
      pulseAnim: new Animated.Value(1),
      delay: i * 120,
    }))
  ).current;

  useEffect(() => {
    nodes.forEach(node => {
      // Fade in
      Animated.timing(node.anim, {
        toValue: 1,
        duration: 800,
        delay: node.delay,
        useNativeDriver: true,
      }).start();

      // Continuous pulse
      const pulse = () => {
        Animated.sequence([
          Animated.timing(node.pulseAnim, {
            toValue: 1.4,
            duration: 1200 + Math.random() * 800,
            delay: Math.random() * 2000,
            useNativeDriver: true,
          }),
          Animated.timing(node.pulseAnim, {
            toValue: 0.8,
            duration: 1200 + Math.random() * 800,
            useNativeDriver: true,
          }),
          Animated.timing(node.pulseAnim, {
            toValue: 1,
            duration: 800,
            useNativeDriver: true,
          }),
        ]).start(() => pulse());
      };
      setTimeout(pulse, node.delay + 800);
    });
  }, []);

  return (
    <View style={[styles.container, { width: SIZE, height: SIZE }]}>
      {/* Connections */}
      {CONNECTIONS.map(([a, b], i) => {
        const na = RAW_NODES[a];
        const nb = RAW_NODES[b];
        const len = distance(na, nb);
        const ang = angle(na, nb);
        const cx = na.x * SIZE;
        const cy = na.y * SIZE;
        return (
          <View
            key={`line-${i}`}
            style={[
              styles.line,
              {
                width: len,
                left: cx,
                top: cy,
                transform: [{ rotate: `${ang}deg` }],
              },
            ]}
          />
        );
      })}

      {/* Nodes */}
      {nodes.map((node, i) => (
        <Animated.View
          key={`node-${i}`}
          style={[
            styles.node,
            {
              width: node.size * 2,
              height: node.size * 2,
              borderRadius: node.size,
              left: node.x * SIZE - node.size,
              top: node.y * SIZE - node.size,
              opacity: node.anim,
              transform: [{ scale: node.pulseAnim }],
              backgroundColor: i === 11 ? '#3B82F6' : i % 3 === 0 ? '#8B5CF6' : '#3B82F6',
              shadowColor: i === 11 ? '#3B82F6' : '#8B5CF6',
              shadowOpacity: 0.9,
              shadowRadius: node.size * 2,
              shadowOffset: { width: 0, height: 0 },
              elevation: 8,
            },
          ]}
        />
      ))}

      {/* Center glow */}
      <View
        style={[
          styles.centerGlow,
          { left: SIZE * 0.5 - 40, top: SIZE * 0.6 - 40 },
        ]}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    position: 'relative',
  },
  line: {
    position: 'absolute',
    height: 1,
    backgroundColor: '#3B82F620',
    transformOrigin: '0% 50%',
  },
  node: {
    position: 'absolute',
  },
  centerGlow: {
    position: 'absolute',
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: '#3B82F608',
  },
});
